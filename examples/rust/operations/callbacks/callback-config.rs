use std::time::Duration;

use aws_durable_execution_sdk as durable;
use serde::Deserialize;
use serde_json::Value;

#[derive(Deserialize)]
struct PaymentEvent {
    amount: f64,
}

async fn handler(
    event: PaymentEvent,
    ctx: durable::DurableContext,
) -> Result<Value, durable::BoxError> {
    let callback = ctx
        .create_callback::<Value>()
        .name("wait-for-payment")
        .timeout(Duration::from_hours(24))
        .heartbeat_timeout(Duration::from_mins(30))
        .await?;

    submit_payment_request(callback.id(), event.amount);
    Ok(callback.result().await?)
}

fn submit_payment_request(_callback_id: &str, _amount: f64) {}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
