use aws_durable_execution_sdk as durable;

fn my_step(arg1: &str, arg2: i32) -> String {
    format!("{arg1}: {arg2}")
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let result = ctx
        .step(|_| async { Ok(my_step("value", 42)) })
        .name("my_step")
        .await?;
    Ok(result)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
