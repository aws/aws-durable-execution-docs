use aws_durable_execution_sdk as durable;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<(), durable::BoxError> {
    ctx.step(|_| async {
        // Records in the step body include operationId and attempt.
        tracing::info!("Running step");
        Ok("done".to_owned())
    })
    .name("process")
    .await?;
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
