use aws_durable_execution_sdk as durable;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<(), durable::BoxError> {
    tracing::info!("This logs on every replay");

    ctx.step(|_| async { Ok("result".to_owned()) })
        .name("step-1")
        .await?;
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    // Install a subscriber without the replay filter to emit logs on every replay.
    lambda_runtime::tracing::init_default_subscriber();
    durable::run(handler).await
}
