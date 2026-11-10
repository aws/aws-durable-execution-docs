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
    // Right: each side effect gets its own step.
    let to_charge = order.clone();
    ctx.step(move |_| async move { Ok(charge_payment(&to_charge)) })
        .name("charge-payment")
        .await?;
    let to_confirm = order.clone();
    ctx.step(move |_| async move { Ok(send_confirmation_email(&to_confirm)) })
        .name("send-confirmation")
        .await?;
    ctx.step(move |_| async move { Ok(update_inventory(&order)) })
        .name("update-inventory")
        .await?;
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
