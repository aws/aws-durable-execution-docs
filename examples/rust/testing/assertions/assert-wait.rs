use std::time::Duration;

use aws_durable_execution_sdk as durable;
use durable::test_util::{ExecutionStatus, LocalRunner, TestOperationDetails};
use durable::{OperationKind, OperationStatus};

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    ctx.wait(Duration::from_secs(30)).name("my-wait").await?;
    Ok("done".to_owned())
}

#[tokio::test]
async fn asserts_on_a_wait_operation() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, serde_json::json!({})).await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);

    let wait = result.operation("my-wait").ok_or("my-wait not found")?;
    assert_eq!(wait.kind(), OperationKind::Wait);
    assert_eq!(wait.status(), OperationStatus::Succeeded);
    let TestOperationDetails::Wait(details) = wait.details() else {
        return Err("not a wait".into());
    };
    assert_eq!(details.duration(), Some(Duration::from_secs(30)));
    assert!(details.scheduled_end_time().is_some());
    Ok(())
}
