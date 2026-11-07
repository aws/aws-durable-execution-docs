use aws_durable_execution_sdk as durable;
use durable::test_util::{ExecutionStatus, LocalRunner};
use durable::{OperationKind, OperationStatus};

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<i32, durable::BoxError> {
    Ok(ctx.step(|_| async { Ok(42) }).name("compute").await?)
}

#[tokio::test]
async fn asserts_on_a_step_operation() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, serde_json::json!({})).await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);

    let step = result.operation("compute").ok_or("compute not found")?;
    assert_eq!(step.kind(), OperationKind::Step);
    assert_eq!(step.status(), OperationStatus::Succeeded);
    let value: i32 = serde_json::from_str(step.result().ok_or("no step result")?)?;
    assert_eq!(value, 42);
    Ok(())
}
