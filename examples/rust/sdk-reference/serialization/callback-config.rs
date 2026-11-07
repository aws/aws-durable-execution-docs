use aws_durable_execution_sdk as durable;
use durable::serdes::{Serdes, SerdesContext};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
struct ApprovalResult {
    approved: bool,
    reason: String,
}

struct ApprovalSerdes;

impl Serdes<ApprovalResult> for ApprovalSerdes {
    fn serialize(
        &self,
        value: ApprovalResult,
        _context: SerdesContext,
    ) -> impl Future<Output = Result<String, durable::BoxError>> + Send {
        std::future::ready(serde_json::to_string(&value).map_err(Into::into))
    }

    fn deserialize(
        &self,
        wire: String,
        _context: SerdesContext,
    ) -> impl Future<Output = Result<ApprovalResult, durable::BoxError>> + Send {
        std::future::ready(serde_json::from_str(&wire).map_err(Into::into))
    }
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<ApprovalResult, durable::BoxError> {
    let approval = ctx
        .create_callback::<ApprovalResult>()
        .name("await-approval")
        .serdes(ApprovalSerdes)
        .await?;

    // Send the callback ID to the external system here.
    tracing::info!("Callback ID: {}", approval.id());

    let result = approval.result().await?;
    Ok(result)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
