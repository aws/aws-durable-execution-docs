use aws_durable_execution_sdk as durable;
use serde::{Deserialize, Serialize};

#[derive(Clone, Deserialize)]
struct Order {
    id: String,
    total: f64,
}

#[derive(Clone, Serialize, Deserialize)]
struct Receipt {
    id: String,
    amount: f64,
}

fn validate(order: &Order) -> bool {
    order.total > 0.0
}

fn charge(order: &Order) -> Receipt {
    Receipt {
        id: format!("rcpt-{}", order.id),
        amount: order.total,
    }
}

fn schedule(_order: &Order, receipt: &Receipt) -> String {
    format!("ship-{}", receipt.id)
}

async fn handler(order: Order, ctx: durable::DurableContext) -> Result<String, durable::BoxError> {
    let result = ctx
        .run_in_child_context(move |child| async move {
            let to_validate = order.clone();
            child
                .step(move |_| async move { Ok(validate(&to_validate)) })
                .name("validate")
                .await?;
            let to_charge = order.clone();
            let receipt = child
                .step(move |_| async move { Ok(charge(&to_charge)) })
                .name("charge")
                .await?;
            child
                .step(move |_| async move { Ok(schedule(&order, &receipt)) })
                .name("schedule")
                .await?;
            Ok("ok".to_owned())
        })
        .name("process-order")
        .await?;
    Ok(result)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
