use std::error::Error as _;
use std::time::Duration;

use aws_durable_execution_sdk as durable;
use durable::builders::RetryStrategyConfig;
use serde::Deserialize;

#[derive(Debug)]
struct TransientApiError;

impl std::fmt::Display for TransientApiError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str("transient API error")
    }
}

impl std::error::Error for TransientApiError {}

#[derive(Debug)]
struct RateLimitError;

impl std::fmt::Display for RateLimitError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str("rate limited")
    }
}

impl std::error::Error for RateLimitError {}

#[derive(Deserialize)]
struct ApiEvent {
    id: String,
}

fn external_api_get(id: &str) -> String {
    format!("record-{id}")
}

async fn handler(event: ApiEvent, ctx: durable::DurableContext) -> Result<(), durable::BoxError> {
    // Retry only transient errors. Anything else (bad input, not-found) fails immediately.
    let retry_strategy = RetryStrategyConfig::builder()
        .max_attempts(5)
        .initial_delay(Duration::from_secs(2))
        .retry_if(|error| {
            error.source().is_some_and(|source| {
                source.is::<TransientApiError>() || source.is::<RateLimitError>()
            })
        })
        .build();

    let id = event.id;
    ctx.step(move |_| async move { Ok(external_api_get(&id)) })
        .name("call-api")
        .retry_strategy_config(retry_strategy)
        .await?;
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
