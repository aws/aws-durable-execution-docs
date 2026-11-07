use aws_durable_execution_sdk as durable;

fn work() -> String {
    "worked".to_owned()
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<(), durable::BoxError> {
    // Wrong: calling ctx.step inside a step body is invalid.
    let outer = ctx.clone();
    ctx.step(move |_| async move {
        outer.step(|_| async { Ok(work()) }).name("inner").await?;
        Ok(())
    })
    .name("outer")
    .await?;
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
