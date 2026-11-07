use aws_durable_execution_sdk as durable;

fn validate() -> bool {
    true
}

fn charge() -> String {
    "charged".to_owned()
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<(), durable::BoxError> {
    // Right: group durable operations in a child context.
    ctx.run_in_child_context(|child| async move {
        child
            .step(|_| async { Ok(validate()) })
            .name("validate")
            .await?;
        child
            .step(|_| async { Ok(charge()) })
            .name("charge")
            .await?;
        Ok("done".to_owned())
    })
    .name("order-pipeline")
    .await?;
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
