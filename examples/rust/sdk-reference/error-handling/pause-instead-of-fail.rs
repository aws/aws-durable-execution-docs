use aws_durable_execution_sdk as durable;
use durable::RetryDecision;
use std::time::Duration;

const PAUSE_FROM_ATTEMPT: u32 = 3;

fn retry_strategy(_error: &durable::StepError, attempt: u32) -> RetryDecision {
    RetryDecision::retry(Duration::from_secs(5)).with_pause(attempt >= PAUSE_FROM_ATTEMPT)
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let result = ctx
        .step(|_| async { Ok(charge_payment()) })
        .name("charge-payment")
        .retry_strategy(retry_strategy)
        .await?;
    Ok(result)
}

fn charge_payment() -> String {
    "ok".to_owned()
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
