use std::time::Duration;

use aws_durable_execution_sdk as durable;
use serde::Serialize;

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct Response {
    status_code: u16,
    body: String,
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Response, durable::BoxError> {
    let message = ctx
        .step(|_| async {
            tracing::info!("Hello from step-1");
            Ok("Hello from Durable Lambda!".to_owned())
        })
        .name("step-1")
        .await?;

    // Pause for 10 seconds without consuming CPU or incurring usage charges
    ctx.wait(Duration::from_secs(10)).await?;

    // Replay-aware: logs once even though the function replays after the wait
    tracing::info!("Resumed after wait");

    Ok(Response {
        status_code: 200,
        body: message,
    })
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
