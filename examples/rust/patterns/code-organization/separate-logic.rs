use aws_durable_execution_sdk as durable;
use serde::{Deserialize, Serialize};

#[derive(Clone, Deserialize)]
#[serde(rename_all = "camelCase")]
struct Order {
    id: String,
    total: f64,
    card_token: String,
    address: String,
}

#[derive(Serialize, Deserialize)]
struct ValidationResult {
    valid: bool,
}

#[derive(Serialize, Deserialize)]
struct Receipt {
    id: String,
    amount: f64,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct OrderResult {
    receipt: Receipt,
    shipment_id: String,
}

// Business logic: no DurableContext, pure work.
fn validate_order(order: &Order) -> ValidationResult {
    order_validator_validate(order)
}

fn charge_payment(order: &Order) -> Receipt {
    payment_service_charge(order.total, &order.card_token)
}

fn schedule_shipment(order: &Order) -> String {
    shipment_service_schedule(&order.id, &order.address)
}

// Orchestration: the handler reads as a sequence of intent.
async fn handler(
    order: Order,
    ctx: durable::DurableContext,
) -> Result<OrderResult, durable::BoxError> {
    let to_validate = order.clone();
    ctx.step(move |_| async move { Ok(validate_order(&to_validate)) })
        .name("validate")
        .await?;
    let to_charge = order.clone();
    let receipt = ctx
        .step(move |_| async move { Ok(charge_payment(&to_charge)) })
        .name("charge")
        .await?;
    let shipment_id = ctx
        .step(move |_| async move { Ok(schedule_shipment(&order)) })
        .name("schedule")
        .await?;
    Ok(OrderResult {
        receipt,
        shipment_id,
    })
}

fn order_validator_validate(order: &Order) -> ValidationResult {
    ValidationResult {
        valid: order.total > 0.0,
    }
}

fn payment_service_charge(total: f64, card_token: &str) -> Receipt {
    Receipt {
        id: format!("rcpt-{card_token}"),
        amount: total,
    }
}

fn shipment_service_schedule(order_id: &str, _address: &str) -> String {
    format!("ship-{order_id}")
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
