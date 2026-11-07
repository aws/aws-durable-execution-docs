use aws_durable_execution_sdk as durable;
use durable::builders::{Backoff, RetryStrategyConfig};
use std::time::Duration;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let retry_strategy = RetryStrategyConfig::builder()
        .max_attempts(5)
        .initial_delay(Duration::from_secs(2))
        .backoff(Backoff::linear(Duration::from_secs(3)))
        .max_delay(Duration::from_secs(30))
        .build();

    let result = ctx
        .step(|_| async { Ok(call_external_api()) })
        .name("call-external-api")
        .retry_strategy_config(retry_strategy)
        .await?;
    Ok(result)
}

fn call_external_api() -> String {
    "ok".to_owned()
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
