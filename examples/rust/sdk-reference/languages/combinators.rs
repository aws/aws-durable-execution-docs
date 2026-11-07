use aws_durable_execution_sdk as durable;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<u32, durable::BoxError> {
    let a = ctx.step(|_| async { Ok(1_u32) }).name("a").future();
    let b = ctx.step(|_| async { Ok(2_u32) }).name("b").future();

    let values = ctx.try_join_all([a, b]).name("all").await?;

    Ok(values.iter().sum())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
