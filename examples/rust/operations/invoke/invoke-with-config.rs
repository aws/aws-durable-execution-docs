use aws_durable_execution_sdk as durable;
use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct OrderEvent {
    order_id: String,
}

#[derive(Serialize, Deserialize)]
struct OrderStatus {
    status: String,
}

async fn handler(
    event: OrderEvent,
    ctx: durable::DurableContext,
) -> Result<OrderStatus, durable::BoxError> {
    let result = ctx
        .invoke::<OrderStatus, _>(
            "order-processor-function:live",
            serde_json::json!({ "orderId": event.order_id }),
        )
        .name("process-order")
        .tenant_id(event.order_id)
        .await?;

    Ok(result)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
