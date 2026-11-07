use aws_durable_execution_sdk as durable;
use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
struct OrderEvent {
    order_id: String,
}

#[derive(Serialize, Deserialize)]
struct Validation {
    order_id: String,
    valid: bool,
}

fn validate_order(_ctx: durable::StepContext, order_id: String) -> Validation {
    Validation {
        order_id,
        valid: true,
    }
}

async fn handler(
    event: OrderEvent,
    ctx: durable::DurableContext,
) -> Result<Validation, durable::BoxError> {
    let order_id = event.order_id;
    let validation = ctx
        .step(move |step_ctx| async move { Ok(validate_order(step_ctx, order_id)) })
        .name("validate_order")
        .await?;
    Ok(validation)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
