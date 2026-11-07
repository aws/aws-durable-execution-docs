use aws_durable_execution_sdk as durable;
use durable::builders::map_parallel::{
    BatchStats, CompletionConfig, CompletionDecision, CompletionOutcome,
};

async fn try_primary(ctx: durable::DurableContext) -> Result<String, durable::BoxError> {
    Ok(ctx
        .step(|_| async { Ok("primary result".to_owned()) })
        .name("primary")
        .await?)
}

async fn try_secondary(ctx: durable::DurableContext) -> Result<String, durable::BoxError> {
    Ok(ctx
        .step(|_| async { Ok("secondary result".to_owned()) })
        .name("secondary")
        .await?)
}

fn stop_after_first_success(stats: &BatchStats<'_>) -> CompletionDecision {
    if stats.succeeded() >= 1 {
        return CompletionDecision::complete(CompletionOutcome::Succeeded);
    }
    CompletionDecision::Continue
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Option<String>, durable::BoxError> {
    let results = ctx
        .parallel([
            durable::Branch::unnamed(try_primary),
            durable::Branch::unnamed(try_secondary),
        ])
        .name("fetch-data")
        .completion(CompletionConfig::with_completion_predicate(
            stop_after_first_success,
        ))
        .await?;

    Ok(results.into_iter().next())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
