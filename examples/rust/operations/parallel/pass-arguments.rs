use aws_durable_execution_sdk as durable;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Vec<String>, durable::BoxError> {
    let items = ["a", "b", "c"];

    let results = ctx
        .parallel(items.map(|item| {
            durable::Branch::unnamed(move |ctx| async move {
                Ok(ctx
                    .step(move |_| async move { Ok(format!("processed {item}")) })
                    .name(format!("process-{item}"))
                    .await?)
            })
        }))
        .name("process-items")
        .await?;

    Ok(results)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
