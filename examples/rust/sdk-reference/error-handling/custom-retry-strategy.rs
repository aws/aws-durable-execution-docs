use aws_durable_execution_sdk as durable;
use durable::RetryDecision;
use std::time::Duration;

fn custom_retry_strategy(_error: &durable::StepError, attempt: u32) -> RetryDecision {
    if attempt >= 4 {
        return RetryDecision::Stop;
    }
    RetryDecision::retry(Duration::from_secs(2))
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let result = ctx
        .step(|_| async { Ok(call_api()) })
        .name("call-api")
        .retry_strategy(custom_retry_strategy)
        .await?;
    Ok(result)
}

fn call_api() -> String {
    "ok".to_owned()
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
