use std::time::Duration;

use aws_durable_execution_sdk as durable;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let name = ctx
        .step(|_| async { Ok("world".to_owned()) })
        .name("fetch-name")
        .await?;

    ctx.wait(Duration::from_secs(2)).name("cooldown").await?;

    let greeting = ctx
        .step(move |_| async move { Ok(format!("hello, {name}")) })
        .name("format")
        .await?;

    Ok(greeting)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
