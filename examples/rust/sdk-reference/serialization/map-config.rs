use aws_durable_execution_sdk as durable;
use durable::serdes::{Serdes, SerdesContext};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
struct ProcessedItem {
    id: String,
    status: String,
}

struct ItemSerdes;

impl Serdes<ProcessedItem> for ItemSerdes {
    fn serialize(
        &self,
        value: ProcessedItem,
        _context: SerdesContext,
    ) -> impl Future<Output = Result<String, durable::BoxError>> + Send {
        std::future::ready(serde_json::to_string(&value).map_err(Into::into))
    }

    fn deserialize(
        &self,
        wire: String,
        _context: SerdesContext,
    ) -> impl Future<Output = Result<ProcessedItem, durable::BoxError>> + Send {
        std::future::ready(serde_json::from_str(&wire).map_err(Into::into))
    }
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Vec<ProcessedItem>, durable::BoxError> {
    let items = ["a", "b", "c"];
    let results = ctx
        .map(items, |_ctx, item, _index| async move {
            Ok(ProcessedItem {
                id: item.to_owned(),
                status: "done".to_owned(),
            })
        })
        .name("process-items")
        .serdes(ItemSerdes)
        .await?;
    Ok(results)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
