use aws_durable_execution_sdk as durable;
use durable::builders::RetryStrategyConfig;

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let receipt = ctx
        .step(|_| async {
            Err::<String, durable::BoxError>(Box::new(durable::TypedError::with_type(
                "CardDeclinedError",
                "card declined",
            )))
        })
        .name("charge")
        .retry_strategy_config(RetryStrategyConfig::no_retry())
        .await;

    match receipt {
        Ok(receipt) => Ok(receipt),
        Err(err)
            if err.wire().and_then(durable::WireError::error_type) == Some("CardDeclinedError") =>
        {
            Ok("declined".to_owned())
        }
        Err(err) => Err(err.into()),
    }
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
