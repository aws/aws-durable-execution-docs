use aws_durable_execution_sdk as durable;
use durable::StepSemantics;
use durable::builders::RetryStrategyConfig;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
struct Processed {
    processed: serde_json::Value,
    status: String,
}

fn process_data(data: serde_json::Value) -> Processed {
    Processed {
        processed: data,
        status: "completed".to_owned(),
    }
}

async fn handler(
    event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Processed, durable::BoxError> {
    let data = event.get("data").cloned().unwrap_or_default();
    let result = ctx
        .step(move |_| async move { Ok(process_data(data)) })
        .name("process_data")
        .retry_strategy_config(RetryStrategyConfig::builder().max_attempts(3).build())
        .semantics(StepSemantics::AtLeastOncePerRetry)
        .await?;
    Ok(result)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
