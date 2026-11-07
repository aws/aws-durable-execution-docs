use aws_durable_execution_sdk as durable;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    tracing::info!("Starting workflow");

    let result = ctx
        .step(|_| async { Ok("done".to_owned()) })
        .name("process")
        .await?;

    tracing::info!(result = %result, "Workflow complete");
    Ok(result)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
