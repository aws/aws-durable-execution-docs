use std::time::Duration;

use aws_durable_execution_sdk as durable;

fn process_data(_event: &serde_json::Value) -> String {
    "processed".to_owned()
}

async fn handler(
    event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let wait = ctx.wait(Duration::from_secs(5)).name("min-delay").spawn();
    let step = ctx
        .step(move |_| async move { Ok(process_data(&event)) })
        .name("process")
        .spawn();

    // At least 5 seconds elapse before both operations complete.
    let (waited, result) = tokio::join!(wait, step);
    waited?;

    Ok(result?)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
