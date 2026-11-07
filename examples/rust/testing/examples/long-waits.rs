use std::time::Duration;

use aws_durable_execution_sdk as durable;
use durable::OperationKind;
use durable::test_util::{ExecutionStatus, LocalRunner};

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    ctx.wait(Duration::from_hours(24))
        .name("cooling-off")
        .await?;
    Ok(ctx
        .step(|_| async { Ok("done".to_owned()) })
        .name("after-wait")
        .await?)
}

#[tokio::test]
async fn completes_instantly_with_time_skipping_enabled() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, serde_json::json!({})).await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);
    assert_eq!(result.output().map(String::as_str), Some("done"));

    let wait = result
        .operation("cooling-off")
        .ok_or("cooling-off not found")?;
    assert_eq!(wait.kind(), OperationKind::Wait);
    assert_eq!(wait.wait_duration().map(|d| d.as_secs()), Some(86_400));
    Ok(())
}
