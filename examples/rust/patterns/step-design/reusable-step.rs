use aws_durable_execution_sdk as durable;
use serde::{Deserialize, Serialize};

#[derive(Clone, Deserialize)]
struct Order {
    id: String,
    total: f64,
}

#[derive(Serialize, Deserialize)]
struct ValidationResult {
    order_id: String,
    valid: bool,
}

fn validate_order(order: &Order) -> ValidationResult {
    ValidationResult {
        order_id: order.id.clone(),
        valid: order.total > 0.0,
    }
}

async fn handler(order: Order, ctx: durable::DurableContext) -> Result<(), durable::BoxError> {
    ctx.step(move |_| async move { Ok(validate_order(&order)) })
        .name("validate-order")
        .await?;
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
