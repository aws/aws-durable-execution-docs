use aws_durable_execution_sdk as durable;
use durable::test_util::{ExecutionStatus, LocalRunner};

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let result = ctx
        .step(|_| async { Ok("hello".to_owned()) })
        .name("greet")
        .await?;
    Ok(result)
}

#[tokio::test]
async fn returns_the_expected_result() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, serde_json::json!({})).await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);
    assert_eq!(result.output().map(String::as_str), Some("hello"));
    Ok(())
}
