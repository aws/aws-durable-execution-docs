use aws_durable_execution_sdk as durable;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Vec<i64>, durable::BoxError> {
    let results = ctx
        .map(vec![1_i64, 2, 3, 4, 5], |ctx, item, index| async move {
            Ok(ctx
                .step(move |_| async move { Ok(item * item) })
                .name(format!("square-{index}"))
                .await?)
        })
        .name("square-numbers")
        .await?;

    Ok(results)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
