use aws_durable_execution_sdk as durable;
use serde::{Deserialize, Serialize};
use serde_json::Value;

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct ApprovalEvent {
    request_id: String,
}

#[derive(Serialize)]
struct ApprovalResult {
    approved: bool,
    result: Value,
}

async fn handler(
    event: ApprovalEvent,
    ctx: durable::DurableContext,
) -> Result<ApprovalResult, durable::BoxError> {
    let callback = ctx
        .create_callback::<Value>()
        .name("wait-for-approval")
        .await?;

    // Send the callback ID to the external system that will resume this function.
    send_approval_request(callback.id(), &event.request_id);

    // Execution suspends here until the external system calls back.
    let result = callback.result().await?;
    Ok(ApprovalResult {
        approved: true,
        result,
    })
}

fn send_approval_request(_callback_id: &str, _request_id: &str) {}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
