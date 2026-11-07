use aws_durable_execution_sdk as durable;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Vec<Vec<String>>, durable::BoxError> {
    let outer = ctx
        .parallel([
            durable::Branch::unnamed(|ctx| async move {
                let inner = ctx
                    .parallel([
                        durable::Branch::unnamed(|c| async move {
                            Ok(c.step(|_| async { Ok("a1".to_owned()) }).name("a1").await?)
                        }),
                        durable::Branch::unnamed(|c| async move {
                            Ok(c.step(|_| async { Ok("a2".to_owned()) }).name("a2").await?)
                        }),
                    ])
                    .name("inner-a")
                    .await?;
                Ok(inner)
            }),
            durable::Branch::unnamed(|ctx| async move {
                let inner = ctx
                    .parallel([
                        durable::Branch::unnamed(|c| async move {
                            Ok(c.step(|_| async { Ok("b1".to_owned()) }).name("b1").await?)
                        }),
                        durable::Branch::unnamed(|c| async move {
                            Ok(c.step(|_| async { Ok("b2".to_owned()) }).name("b2").await?)
                        }),
                    ])
                    .name("inner-b")
                    .await?;
                Ok(inner)
            }),
        ])
        .name("outer")
        .await?;

    Ok(outer)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
