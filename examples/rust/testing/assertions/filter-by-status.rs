use std::sync::atomic::{AtomicU32, Ordering};

use aws_durable_execution_sdk as durable;
use durable::OperationStatus;
use durable::builders::RetryStrategyConfig;
use durable::test_util::{ExecutionStatus, LocalRunner, TestOperationDetails};

static CALL_COUNT: AtomicU32 = AtomicU32::new(0);

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    Ok(ctx
        .step(|_| async {
            if CALL_COUNT.fetch_add(1, Ordering::SeqCst) + 1 < 3 {
                return Err("not yet".into());
            }
            Ok("ok".to_owned())
        })
        .name("flaky")
        .retry_strategy_config(RetryStrategyConfig::builder().max_attempts(3).build())
        .await?)
}

#[tokio::test]
async fn filters_operations_by_status() -> Result<(), durable::BoxError> {
    CALL_COUNT.store(0, Ordering::SeqCst);
    let runner = LocalRunner::new();
    let result = runner.run(handler, serde_json::json!({})).await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);

    let failed_ops = result
        .operations()
        .iter()
        .filter(|op| op.status() == OperationStatus::Failed)
        .count();
    assert_eq!(failed_ops, 0);

    let succeeded_ops = result
        .operations()
        .iter()
        .filter(|op| op.status() == OperationStatus::Succeeded)
        .count();
    assert_eq!(succeeded_ops, 1);

    let flaky = result.operation("flaky").ok_or("flaky not found")?;
    let TestOperationDetails::Step(details) = flaky.details() else {
        return Err("not a step".into());
    };
    assert_eq!(details.attempt(), 2);
    Ok(())
}
