use aws_durable_execution_sdk as durable;
use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct OrderEvent {
    order_id: String,
}

#[derive(Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct ValidatedOrder {
    order_id: String,
    valid: bool,
}

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct ChargedOrder {
    order_id: String,
    valid: bool,
    charged: bool,
}

async fn handler(
    event: OrderEvent,
    ctx: durable::DurableContext,
) -> Result<ChargedOrder, durable::BoxError> {
    let order_id = event.order_id;
    let result = ctx
        .run_in_child_context(|child| async move {
            let step1 = child
                .step(move |_| async move { Ok(validate(order_id)) })
                .name("validate")
                .await?;
            let step2 = child
                .step(move |_| async move { Ok(charge(step1)) })
                .name("charge")
                .await?;
            Ok(step2)
        })
        .name("process-order")
        .await?;
    Ok(result)
}

fn validate(order_id: String) -> ValidatedOrder {
    ValidatedOrder {
        order_id,
        valid: true,
    }
}

fn charge(order: ValidatedOrder) -> ChargedOrder {
    ChargedOrder {
        order_id: order.order_id,
        valid: order.valid,
        charged: true,
    }
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
