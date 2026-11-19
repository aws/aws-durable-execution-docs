use aws_durable_execution_sdk as durable;
use durable::RetryDecision;
use std::error::Error as _;
use std::time::Duration;

#[derive(Debug)]
struct DownstreamUnavailableError;

impl std::fmt::Display for DownstreamUnavailableError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str("downstream unavailable")
    }
}

impl std::error::Error for DownstreamUnavailableError {}

fn retry_strategy(error: &durable::StepError, attempt: u32) -> RetryDecision {
    if attempt >= 10 {
        return RetryDecision::Stop;
    }
    let unavailable = error
        .source()
        .and_then(|source| source.downcast_ref::<DownstreamUnavailableError>())
        .is_some();
    RetryDecision::retry(Duration::from_secs(2)).with_pause(attempt == 5 && unavailable)
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let result = ctx
        .step(|_| async { Ok(call_downstream()) })
        .name("call-downstream")
        .retry_strategy(retry_strategy)
        .await?;
    Ok(result)
}

fn call_downstream() -> String {
    "ok".to_owned()
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
