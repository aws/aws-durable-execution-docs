use aws_durable_execution_sdk as durable;
use durable::OperationErrorKind;
use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct PaymentEvent {
    order_id: String,
}

#[derive(Serialize)]
struct PaymentOutcome {
    status: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    result: Option<serde_json::Value>,
    #[serde(skip_serializing_if = "Option::is_none")]
    reason: Option<String>,
}

async fn handler(
    event: PaymentEvent,
    ctx: durable::DurableContext,
) -> Result<PaymentOutcome, durable::BoxError> {
    let invoked = ctx
        .invoke::<serde_json::Value>(
            "payment-processor-function:live",
            serde_json::json!({ "orderId": event.order_id }),
        )
        .name("process-payment")
        .await;

    match invoked {
        Ok(result) => Ok(PaymentOutcome {
            status: "success".to_owned(),
            result: Some(result),
            reason: None,
        }),
        Err(err) => match err.kind() {
            OperationErrorKind::Invoke(_) => {
                let reason = err
                    .wire()
                    .and_then(|wire| wire.error_message())
                    .map_or_else(|| format!("{err:#}"), str::to_owned);
                Ok(PaymentOutcome {
                    status: "failed".to_owned(),
                    result: None,
                    reason: Some(reason),
                })
            }
            _ => Err(err.into()),
        },
    }
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
