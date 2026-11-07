use aws_durable_execution_sdk as durable;
use durable::builders::RetryStrategyConfig;
use serde::Serialize;

#[derive(Serialize)]
struct Output {
    result: String,
    audit: String,
    critical: String,
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Output, durable::BoxError> {
    // Default: 6 attempts, 5s initial delay, 60s max, 2x backoff, full jitter
    let result = ctx
        .step(|_| async { Ok(call_api()) })
        .name("call-api")
        .retry_strategy_config(RetryStrategyConfig::default())
        .await?;

    // Linear: 6 attempts, delays of 1s, 2s, 3s, 4s, 5s
    let audit = ctx
        .step(|_| async { Ok(write_audit_log()) })
        .name("audit-log")
        .retry_strategy_config(RetryStrategyConfig::linear())
        .await?;

    let critical = ctx
        .step(|_| async { Ok(charge_payment()) })
        .name("charge-payment")
        .retry_strategy_config(RetryStrategyConfig::no_retry())
        .await?;

    Ok(Output {
        result,
        audit,
        critical,
    })
}

fn call_api() -> String {
    "ok".to_owned()
}

fn write_audit_log() -> String {
    "logged".to_owned()
}

fn charge_payment() -> String {
    "charged".to_owned()
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
