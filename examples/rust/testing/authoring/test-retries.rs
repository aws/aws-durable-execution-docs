use std::sync::atomic::{AtomicU32, Ordering};

use aws_durable_execution_sdk as durable;
use durable::builders::RetryStrategyConfig;
use durable::test_util::{ExecutionStatus, LocalRunner};

static ATTEMPTS: AtomicU32 = AtomicU32::new(0);

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    Ok(ctx
        .step(|_| async {
            if ATTEMPTS.fetch_add(1, Ordering::SeqCst) + 1 < 3 {
                return Err("transient error".into());
            }
            Ok("done".to_owned())
        })
        .name("flaky")
        .retry_strategy_config(RetryStrategyConfig::builder().max_attempts(3).build())
        .await?)
}

#[tokio::test]
async fn retries_and_eventually_succeeds() -> Result<(), durable::BoxError> {
    ATTEMPTS.store(0, Ordering::SeqCst);
    let runner = LocalRunner::new();
    let result = runner.run(handler, serde_json::json!({})).await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);
    assert_eq!(result.output().map(String::as_str), Some("done"));
    Ok(())
}
