use std::sync::{Arc, Mutex};

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

// Wrong: the step body pushes to an outer list. Replay returns the
// cached result without running the body, so the list stays empty
// and the handler returns {"receipts": []} on replay.
async fn handler(
    event: SaveEvent,
    ctx: durable::DurableContext,
) -> Result<SaveResult, durable::BoxError> {
    let receipts = Arc::new(Mutex::new(Vec::new()));
    for item in event.items {
        let name = format!("save-{}", item.id);
        let shared = Arc::clone(&receipts);
        ctx.step(move |_| async move {
            let receipt = save_item(&item);
            shared.lock().map_err(|e| e.to_string())?.push(receipt.id);
            Ok(())
        })
        .name(name)
        .await?;
    }
    let receipts = receipts.lock().map_err(|e| e.to_string())?.clone();
    Ok(SaveResult { receipts })
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
