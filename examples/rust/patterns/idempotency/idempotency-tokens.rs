use aws_durable_execution_sdk as durable;
use serde::Deserialize;

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct ChargeEvent {
    amount: f64,
    card_token: String,
}

struct ChargeRequest {
    amount: f64,
    card_token: String,
    idempotency_key: String,
}

fn payment_service_charge(request: &ChargeRequest) -> String {
    format!(
        "charged {} to {} with key {}",
        request.amount, request.card_token, request.idempotency_key
    )
}

async fn handler(
    event: ChargeEvent,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let idempotency_key = ctx
        .step(|_| async { Ok(uuid::Uuid::new_v4().to_string()) })
        .name("idempotency-key")
        .await?;

    let charge = ctx
        .step(move |_| async move {
            Ok(payment_service_charge(&ChargeRequest {
                amount: event.amount,
                card_token: event.card_token,
                idempotency_key,
            }))
        })
        .name("charge")
        .await?;
    Ok(charge)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
