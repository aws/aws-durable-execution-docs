use aws_durable_execution_sdk as durable;

fn add_numbers(_ctx: durable::StepContext, a: i32, b: i32) -> i32 {
    a + b
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<i32, durable::BoxError> {
    let result = ctx
        .step(|step_ctx| async move { Ok(add_numbers(step_ctx, 5, 3)) })
        .name("add_numbers")
        .await?;
    Ok(result)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
