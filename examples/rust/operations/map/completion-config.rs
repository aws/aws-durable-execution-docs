use aws_durable_execution_sdk as durable;
use durable::builders::map_parallel::CompletionConfig;
use serde::Deserialize;

#[derive(Deserialize)]
struct ItemsEvent {
    items: Vec<String>,
}

async fn handler(
    event: ItemsEvent,
    ctx: durable::DurableContext,
) -> Result<Vec<String>, durable::BoxError> {
    let results = ctx
        .map(event.items, |ctx, item, index| async move {
            Ok(ctx
                .step(move |_| async move { Ok(item.to_uppercase()) })
                .name(format!("process-{index}"))
                .await?)
        })
        .name("process-items")
        .completion(CompletionConfig::with_min_successful(3))
        .await?;

    Ok(results)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
