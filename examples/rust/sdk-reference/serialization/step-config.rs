use aws_durable_execution_sdk as durable;
use durable::serdes::{Serdes, SerdesContext};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
struct Order {
    id: String,
    total: String,
}

struct OrderSerdes;

impl Serdes<Order> for OrderSerdes {
    fn serialize(
        &self,
        value: Order,
        _context: SerdesContext,
    ) -> impl Future<Output = Result<String, durable::BoxError>> + Send {
        std::future::ready(serde_json::to_string(&value).map_err(Into::into))
    }

    fn deserialize(
        &self,
        wire: String,
        _context: SerdesContext,
    ) -> impl Future<Output = Result<Order, durable::BoxError>> + Send {
        std::future::ready(serde_json::from_str(&wire).map_err(Into::into))
    }
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Order, durable::BoxError> {
    let order = ctx
        .step(|_| async {
            Ok(Order {
                id: "order-123".to_owned(),
                total: "99.99".to_owned(),
            })
        })
        .name("fetch-order")
        .serdes(OrderSerdes)
        .await?;
    Ok(order)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
