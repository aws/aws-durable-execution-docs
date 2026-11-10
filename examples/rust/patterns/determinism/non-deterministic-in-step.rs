use aws_durable_execution_sdk as durable;
use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
struct ChargeEvent {
    amount: f64,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct ChargeResult {
    transaction_id: String,
    receipt: String,
}

fn charge(_amount: f64, transaction_id: &str) -> String {
    format!("rcpt-{transaction_id}")
}

async fn handler(
    event: ChargeEvent,
    ctx: durable::DurableContext,
) -> Result<ChargeResult, durable::BoxError> {
    let transaction_id = ctx
        .step(|_| async { Ok(uuid::Uuid::new_v4().to_string()) })
        .name("generate-transaction-id")
        .await?;

    let id = transaction_id.clone();
    let receipt = ctx
        .step(move |_| async move { Ok(charge(event.amount, &id)) })
        .name("charge")
        .await?;

    Ok(ChargeResult {
        transaction_id,
        receipt,
    })
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
