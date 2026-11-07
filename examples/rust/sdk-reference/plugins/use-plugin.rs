use aws_durable_execution_sdk as durable;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let result = ctx
        .step(|_| async { Ok("done".to_owned()) })
        .name("process")
        .await?;
    Ok(result)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    let options = durable::Options::builder()
        .plugin(ExamplePluginFactory)
        .build()?;
    durable::run_with_options(handler, options).await
}
