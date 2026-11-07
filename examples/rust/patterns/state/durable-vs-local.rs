use aws_durable_execution_sdk as durable;
use serde::Serialize;

#[derive(Serialize)]
struct FetchResult {
    value: String,
}

fn fetch_value() -> String {
    "fetched".to_owned()
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<FetchResult, durable::BoxError> {
    // The SDK checkpoints the step's return value as durable state.
    let value = ctx
        .step(|_| async { Ok(fetch_value()) })
        .name("fetch")
        .await?;

    // Assigning or copying the local variable does not add to durable state.
    let alias = &value;
    let _copy = alias.clone();

    // The SDK serializes and stores the handler's return value as durable state.
    Ok(FetchResult { value })
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
