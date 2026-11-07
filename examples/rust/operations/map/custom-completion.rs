use aws_durable_execution_sdk as durable;
use durable::builders::map_parallel::{
    BatchStats, CompletionConfig, CompletionDecision, CompletionOutcome,
};
use serde::Deserialize;

#[derive(Deserialize)]
struct ItemsEvent {
    items: Vec<String>,
}

async fn process_item(
    ctx: durable::DurableContext,
    item: String,
    index: usize,
) -> Result<String, durable::BoxError> {
    Ok(ctx
        .step(move |_| async move { Ok(item.to_uppercase()) })
        .name(format!("process-{index}"))
        .await?)
}

fn stop_after_two_successes(stats: &BatchStats<'_>) -> CompletionDecision {
    if stats.succeeded() >= 2 {
        return CompletionDecision::Complete(CompletionOutcome::Succeeded);
    }
    CompletionDecision::Continue
}

async fn handler(
    event: ItemsEvent,
    ctx: durable::DurableContext,
) -> Result<Vec<String>, durable::BoxError> {
    let results = ctx
        .map(event.items, process_item)
        .name("process-items")
        .completion(CompletionConfig::with_completion_predicate(
            stop_after_two_successes,
        ))
        .await?;

    Ok(results)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
