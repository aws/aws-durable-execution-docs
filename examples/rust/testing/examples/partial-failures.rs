use aws_durable_execution_sdk as durable;
use durable::OperationStatus;
use durable::test_util::{ExecutionStatus, LocalRunner};

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<(), durable::BoxError> {
    ctx.step(|_| async { Ok("ok".to_owned()) })
        .name("step-1")
        .await?;
    ctx.step(|_| async { Ok("ok".to_owned()) })
        .name("step-2")
        .await?;
    ctx.step(|_| async { Err::<(), durable::BoxError>("step-3 failed".into()) })
        .name("step-3")
        .await?;
    Ok(())
}

#[tokio::test]
async fn records_which_steps_succeeded_before_the_failure() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, serde_json::json!({})).await?;

    assert_eq!(result.status(), ExecutionStatus::Failed);

    let step1 = result.operation("step-1").ok_or("step-1 not found")?;
    assert_eq!(step1.status(), OperationStatus::Succeeded);

    let step2 = result.operation("step-2").ok_or("step-2 not found")?;
    assert_eq!(step2.status(), OperationStatus::Succeeded);

    let message = result.error_message().ok_or("no error message")?;
    assert!(message.contains("step-3 failed"));
    Ok(())
}
