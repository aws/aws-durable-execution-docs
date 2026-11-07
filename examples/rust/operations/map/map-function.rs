use aws_durable_execution_sdk as durable;
use serde::{Deserialize, Serialize};

#[derive(Clone, Serialize, Deserialize)]
struct Order {
    id: String,
    amount: f64,
}

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct Receipt {
    order_id: String,
    charged: f64,
}

async fn process_order(
    ctx: durable::DurableContext,
    order: Order,
    _index: usize,
) -> Result<Receipt, durable::BoxError> {
    let validated = ctx
        .step(move |_| async move {
            if order.amount <= 0.0 {
                return Err("Invalid amount".into());
            }
            Ok(order)
        })
        .name("validate")
        .await?;
    let amount = validated.amount;
    let charged = ctx
        .step(move |_| async move { Ok(amount) })
        .name("charge")
        .await?;
    Ok(Receipt {
        order_id: validated.id,
        charged,
    })
}
