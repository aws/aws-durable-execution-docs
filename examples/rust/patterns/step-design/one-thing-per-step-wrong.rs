use aws_durable_execution_sdk as durable;
use serde::Deserialize;

#[derive(Clone, Deserialize)]
struct Order {
    id: String,
}

fn charge_payment(order: &Order) -> String {
    format!("rcpt-{}", order.id)
}

fn send_confirmation_email(order: &Order) -> String {
    format!("email-{}", order.id)
}

fn update_inventory(order: &Order) -> String {
    format!("inventory-{}", order.id)
}

async fn handler(order: Order, ctx: durable::DurableContext) -> Result<(), durable::BoxError> {
    // Wrong: one step for three unrelated side effects.
    ctx.step(move |_| async move {
        charge_payment(&order);
        send_confirmation_email(&order);
        update_inventory(&order);
        Ok(())
    })
    .name("process-order")
    .await?;
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
