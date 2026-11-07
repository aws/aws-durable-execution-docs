use aws_durable_execution_sdk as durable;
use durable::OperationErrorKind;
use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct OrderEvent {
    order_id: Option<String>,
}

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct ProcessedOrder {
    order_id: String,
    status: String,
}

#[derive(Serialize)]
#[serde(untagged)]
enum OrderResult {
    Processed(ProcessedOrder),
    Failed { error: String },
}

async fn handler(
    event: OrderEvent,
    ctx: durable::DurableContext,
) -> Result<OrderResult, durable::BoxError> {
    let order_id = event.order_id;
    let result = ctx
        .step(move |_| async move {
            let order_id = order_id.ok_or("orderId is required")?;
            Ok(ProcessedOrder {
                order_id,
                status: "processed".to_owned(),
            })
        })
        .name("process-order")
        .await;

    match result {
        Ok(order) => Ok(OrderResult::Processed(order)),
        Err(err) => match err.kind() {
            OperationErrorKind::Step(_) => {
                let cause = err.wire().and_then(|wire| wire.error_message());
                tracing::error!(cause, "Step failed");
                Ok(OrderResult::Failed {
                    error: cause.unwrap_or("Step failed").to_owned(),
                })
            }
            _ => Err(err.into()),
        },
    }
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
