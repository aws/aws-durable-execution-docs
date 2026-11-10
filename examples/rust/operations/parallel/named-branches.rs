use aws_durable_execution_sdk as durable;

async fn task_a(ctx: durable::DurableContext) -> Result<String, durable::BoxError> {
    Ok(ctx
        .step(|_| async { Ok("a done".to_owned()) })
        .name("run-a")
        .await?)
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Vec<String>, durable::BoxError> {
    let results = ctx
        .parallel([
            durable::Branch::unnamed(task_a),
            durable::Branch::new("task-b", |ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("b done".to_owned()) })
                    .name("run-b")
                    .await?)
            }),
        ])
        .name("process")
        .await?;

    Ok(results)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
