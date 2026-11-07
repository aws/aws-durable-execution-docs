use aws_durable_execution_sdk as durable;
use durable::builders::map_parallel::{CompletionConfig, NestingMode};

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Option<String>, durable::BoxError> {
    let results = ctx
        .parallel([
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("primary result".to_owned()) })
                    .name("primary")
                    .await?)
            }),
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("secondary result".to_owned()) })
                    .name("secondary")
                    .await?)
            }),
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("cache result".to_owned()) })
                    .name("cache")
                    .await?)
            }),
        ])
        .name("fetch-data")
        .max_concurrency(2)
        .completion(CompletionConfig::with_min_successful(1))
        .nesting(NestingMode::Flat)
        .await?;

    Ok(results.into_iter().next())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
