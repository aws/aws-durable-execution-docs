use std::time::Duration;

use aws_durable_execution_sdk as durable;
use serde::Deserialize;
use serde_json::Value;

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct ApprovalEvent {
    order_id: String,
}

fn approvals_service_request(_id: &str, _callback_id: &str) {}

async fn handler(
    event: ApprovalEvent,
    ctx: durable::DurableContext,
) -> Result<Value, durable::BoxError> {
    let order_id = event.order_id;
    let outcome = ctx
        .wait_for_callback::<Value>(move |_step_ctx, callback_id| async move {
            approvals_service_request(&order_id, &callback_id);
            Ok(())
        })
        .name("wait-for-approval")
        .timeout(Duration::from_hours(24))
        .await?;
    Ok(outcome)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
