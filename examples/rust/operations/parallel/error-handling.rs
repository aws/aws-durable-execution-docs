use aws_durable_execution_sdk as durable;
use durable::builders::map_parallel::CompletionConfig;
use serde::Serialize;

#[derive(Serialize)]
struct Summary {
    succeeded: usize,
    failed: usize,
    results: Vec<String>,
    errors: Vec<String>,
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Summary, durable::BoxError> {
    let result = ctx
        .parallel([
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("ok".to_owned()) })
                    .name("task-1")
                    .await?)
            }),
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Err("task 2 failed".into()) })
                    .name("task-2")
                    .await?)
            }),
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("ok".to_owned()) })
                    .name("task-3")
                    .await?)
            }),
        ])
        .name("tasks")
        .completion(CompletionConfig::with_tolerated_failure_count(1))
        .await_batch()
        .await?;

    Ok(Summary {
        succeeded: result.success_count(),
        failed: result.failure_count(),
        results: result.results().cloned().collect(),
        errors: result.errors().map(|e| e.message.to_owned()).collect(),
    })
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
