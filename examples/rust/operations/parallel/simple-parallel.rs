use aws_durable_execution_sdk as durable;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Vec<String>, durable::BoxError> {
    let results = ctx
        .parallel([
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("inventory ok".to_owned()) })
                    .name("check-inventory")
                    .await?)
            }),
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("payment ok".to_owned()) })
                    .name("check-payment")
                    .await?)
            }),
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("shipping ok".to_owned()) })
                    .name("check-shipping")
                    .await?)
            }),
        ])
        .name("check-services")
        .await?;

    Ok(results)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
