use aws_durable_execution_sdk as durable;
use durable::builders::map_parallel::CompletionConfig;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Option<String>, durable::BoxError> {
    let results = ctx
        .parallel([
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("result from a".to_owned()) })
                    .name("source-a")
                    .await?)
            }),
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("result from b".to_owned()) })
                    .name("source-b")
                    .await?)
            }),
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("result from c".to_owned()) })
                    .name("source-c")
                    .await?)
            }),
        ])
        .name("race")
        .completion(CompletionConfig::with_min_successful(1))
        .await?;

    Ok(results.into_iter().next())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
