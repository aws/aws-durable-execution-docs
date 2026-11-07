use aws_durable_execution_sdk as durable;
use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct OrderEvent {
    order_id: String,
    user_id: String,
}

#[derive(Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct ValidatedOrder {
    order_id: String,
    user_id: String,
}

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct ChargedOrder {
    order_id: String,
    user_id: String,
    charged: bool,
}

async fn handler(
    event: OrderEvent,
    ctx: durable::DurableContext,
) -> Result<ChargedOrder, durable::BoxError> {
    let OrderEvent { order_id, user_id } = event;

    let result = ctx
        .run_in_child_context(|child| async move {
            let validated = child
                .step(move |_| async move { Ok(validate(order_id, user_id)) })
                .name("validate")
                .await?;
            Ok(child
                .step(move |_| async move { Ok(charge(validated)) })
                .name("charge")
                .await?)
        })
        .name("process-order")
        .await?;

    Ok(result)
}

fn validate(order_id: String, user_id: String) -> ValidatedOrder {
    ValidatedOrder { order_id, user_id }
}

fn charge(order: ValidatedOrder) -> ChargedOrder {
    ChargedOrder {
        order_id: order.order_id,
        user_id: order.user_id,
        charged: true,
    }
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
