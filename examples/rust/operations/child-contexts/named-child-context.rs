use aws_durable_execution_sdk as durable;
use serde::Serialize;

#[derive(Serialize)]
struct NamingResult {
    unnamed: String,
    named: String,
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<NamingResult, durable::BoxError> {
    let unnamed = ctx
        .run_in_child_context(|child| async move {
            Ok(child.step(|_| async { Ok("result".to_owned()) }).await?)
        })
        .await?;

    let named = ctx
        .run_in_child_context(|child| async move {
            Ok(child.step(|_| async { Ok("result".to_owned()) }).await?)
        })
        .name("process-order")
        .await?;

    Ok(NamingResult { unnamed, named })
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
