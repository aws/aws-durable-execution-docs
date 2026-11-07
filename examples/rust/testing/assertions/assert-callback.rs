use aws_durable_execution_sdk as durable;
use durable::test_util::{CallbackReply, ExecutionStatus, LocalRunner, WaitingStatus};

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<serde_json::Value, durable::BoxError> {
    Ok(ctx
        .wait_for_callback::<serde_json::Value, _, _>(|_step_ctx, callback_id| async move {
            // In production this would notify an external system
            let _ = callback_id;
            Ok(())
        })
        .name("approval")
        .await?)
}

#[tokio::test]
async fn completes_a_callback_from_the_test() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let run = runner.run(handler, serde_json::json!({}));

    let callback = run.operation("approval");
    callback.wait_for_data(WaitingStatus::Submitted).await?;
    callback
        .send_callback(CallbackReply::success(
            &serde_json::json!({ "approved": true }),
        )?)
        .await?;

    let result = run.await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);

    let approved = result.output().and_then(|output| output.get("approved"));
    assert_eq!(approved, Some(&serde_json::json!(true)));
    Ok(())
}
