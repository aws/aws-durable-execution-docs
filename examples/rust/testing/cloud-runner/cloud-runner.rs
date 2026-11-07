use aws_durable_execution_sdk as durable;
use durable::test_util::{CloudRunner, ExecutionStatus};

#[tokio::test]
async fn runs_against_a_deployed_function() -> Result<(), durable::BoxError> {
    let runner = CloudRunner::new("MyFunction")
        .qualifier("$LATEST")
        .region("us-east-1");

    let result = runner
        .run::<_, String>(serde_json::json!({ "name": "world" }))
        .await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);
    assert_eq!(result.output().map(String::as_str), Some("hello world"));
    Ok(())
}
