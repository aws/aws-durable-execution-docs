use std::time::Duration;

use aws_durable_execution_sdk as durable;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<i32, durable::BoxError> {
    let wait = ctx.wait(Duration::from_secs(5)).name("timer").spawn();
    let work = ctx.step(|_| async { Ok(42_i32) }).name("compute").spawn();

    let (timer, result) = tokio::join!(wait, work);
    timer?;
    let value = result?;

    Ok(value)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
