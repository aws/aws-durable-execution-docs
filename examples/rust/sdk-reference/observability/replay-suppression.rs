use aws_durable_execution_sdk as durable;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<(), durable::BoxError> {
    // The default subscriber suppresses duplicate logs during replay.
    tracing::info!("Step 1 starting");

    ctx.step(|_| async { Ok("result".to_owned()) })
        .name("step-1")
        .await?;

    tracing::info!("Step 1 complete");
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
