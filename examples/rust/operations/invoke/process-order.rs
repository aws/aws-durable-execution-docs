use aws_durable_execution_sdk as durable;
use serde::{Deserialize, Serialize};

#[derive(Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct OrderEvent {
    order_id: String,
    amount: f64,
}

#[derive(Serialize, Deserialize)]
struct ValidationResult {
    valid: bool,
    reason: Option<String>,
}

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct PaymentResult {
    transaction_id: String,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct OrderResult {
    status: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    reason: Option<String>,
    #[serde(skip_serializing_if = "Option::is_none")]
    transaction_id: Option<String>,
}

async fn handler(
    event: OrderEvent,
    ctx: durable::DurableContext,
) -> Result<OrderResult, durable::BoxError> {
    let validation = ctx
        .invoke::<ValidationResult, _>("validate-order-function:live", event.clone())
        .name("validate-order")
        .await?;

    if !validation.valid {
        return Ok(OrderResult {
            status: "rejected".to_owned(),
            reason: validation.reason,
            transaction_id: None,
        });
    }

    let payment = ctx
        .invoke::<PaymentResult, _>("payment-processor-function:live", event)
        .name("process-payment")
        .await?;

    Ok(OrderResult {
        status: "completed".to_owned(),
        reason: None,
        transaction_id: Some(payment.transaction_id),
    })
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
