use aws_durable_execution_sdk as durable;
use durable::builders::RetryStrategyConfig;
use std::error::Error as _;
use std::time::Duration;

#[derive(Debug)]
struct RateLimitError;

impl std::fmt::Display for RateLimitError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str("rate limited")
    }
}

impl std::error::Error for RateLimitError {}

#[derive(Debug)]
struct ServiceUnavailableError;

impl std::fmt::Display for ServiceUnavailableError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str("service unavailable")
    }
}

impl std::error::Error for ServiceUnavailableError {}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let retry_strategy = RetryStrategyConfig::builder()
        .max_attempts(5)
        .initial_delay(Duration::from_secs(2))
        // All other errors fail immediately.
        .retry_if(|error| {
            error.source().is_some_and(|source| {
                source.is::<RateLimitError>() || source.is::<ServiceUnavailableError>()
            })
        })
        .build();

    let result = ctx
        .step(|_| async { Ok(call_api()) })
        .name("call-api")
        .retry_strategy_config(retry_strategy)
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
