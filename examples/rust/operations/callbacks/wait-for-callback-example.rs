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
    let request_id = event.request_id;
    let result = ctx
        .wait_for_callback::<Value, _, _>(move |_step_ctx, callback_id| async move {
            send_approval_request(&callback_id, &request_id);
            Ok(())
        })
        .name("wait-for-approval")
        .await?;
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
