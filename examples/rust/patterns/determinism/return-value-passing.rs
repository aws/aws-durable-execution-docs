use aws_durable_execution_sdk as durable;
use serde::{Deserialize, Serialize};

#[derive(Clone, Deserialize)]
struct Item {
    id: String,
}

#[derive(Deserialize)]
struct SaveEvent {
    items: Vec<Item>,
}

#[derive(Serialize)]
struct SaveResult {
    receipts: Vec<String>,
}

struct Receipt {
    id: String,
}

fn save_item(item: &Item) -> Receipt {
    Receipt {
        id: format!("r-{}", item.id),
    }
}

// Right: the step returns the receipt id. The handler appends the
// returned value to the outer list, which replay rebuilds from the
// cached step results.
async fn handler(
    event: SaveEvent,
    ctx: durable::DurableContext,
) -> Result<SaveResult, durable::BoxError> {
    let mut receipts = Vec::new();
    for item in event.items {
        let name = format!("save-{}", item.id);
        let receipt_id = ctx
            .step(move |_| async move {
                let receipt = save_item(&item);
                Ok(receipt.id)
            })
            .name(name)
            .await?;
        receipts.push(receipt_id);
    }
    Ok(SaveResult { receipts })
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
