use aws_durable_execution_sdk as durable;
use durable::builders::RetryStrategyConfig;
use serde::{Deserialize, Serialize};
use std::time::Duration;

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct OrderEvent {
    order_id: String,
}

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct ChargeRequest {
    order_id: String,
}

async fn handler(
    event: OrderEvent,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let retry_strategy = RetryStrategyConfig::builder()
        .max_attempts(3)
        .initial_delay(Duration::from_secs(2))
        .backoff_rate(2.0)
        .build();

    // invoke takes no retry strategy, so wrap it with with_retry to apply
    // backoff between failed attempts.
    let order_id = event.order_id;
    let receipt = ctx
        .with_retry(move |child, attempt| {
            let request = ChargeRequest {
                order_id: order_id.clone(),
            };
            async move {
                let receipt: String = child
                    .invoke("process-payment", request)
                    .name(format!("charge-{attempt}"))
                    .await?;
                Ok(receipt)
            }
        })
        .name("charge-payment")
        .retry_strategy_config(retry_strategy)
        .await?;
    Ok(receipt)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
