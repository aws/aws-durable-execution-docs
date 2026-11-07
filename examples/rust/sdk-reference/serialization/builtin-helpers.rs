use aws_durable_execution_sdk as durable;
use durable::JsonSerdes;
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
struct Order {
    id: String,
    total: String,
}

impl Order {
    fn label(&self) -> String {
        format!("Order {} — {}", self.id, self.total)
    }
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let order = ctx
        .step(|_| async {
            Ok(Order {
                id: "order-123".to_owned(),
                total: "99.99".to_owned(),
            })
        })
        .name("fetch-order")
        .serdes(JsonSerdes)
        .await?;
    Ok(order.label())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
