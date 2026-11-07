use aws_durable_execution_sdk as durable;
use durable::{OperationErrorKind, StepErrorKind, StepSemantics};
use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
struct PaymentEvent {
    amount: f64,
}

#[derive(Serialize, Deserialize)]
struct Receipt {
    charged: f64,
}

#[derive(Serialize)]
struct PaymentResult {
    status: String,
    #[serde(skip_serializing_if = "Option::is_none")]
    result: Option<Receipt>,
}

async fn handler(
    event: PaymentEvent,
    ctx: durable::DurableContext,
) -> Result<PaymentResult, durable::BoxError> {
    let amount = event.amount;
    let result = ctx
        .step(move |_| async move { Ok(charge_payment(amount)) })
        .name("charge-payment")
        .semantics(StepSemantics::AtMostOncePerRetry)
        .await;

    match result {
        Ok(receipt) => Ok(PaymentResult {
            status: "charged".to_owned(),
            result: Some(receipt),
        }),
        Err(err) => match err.kind() {
            OperationErrorKind::Step(step_err)
                if matches!(step_err.kind(), StepErrorKind::Interrupted { .. }) =>
            {
                // Lambda interrupted the step before the SDK checkpointed the result,
                // and no retry remains. Check your payment system to determine
                // whether the charge succeeded.
                tracing::warn!("Payment step interrupted; check payment system");
                Ok(PaymentResult {
                    status: "unknown".to_owned(),
                    result: None,
                })
            }
            _ => Err(err.into()),
        },
    }
}

fn charge_payment(amount: f64) -> Receipt {
    Receipt { charged: amount }
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
