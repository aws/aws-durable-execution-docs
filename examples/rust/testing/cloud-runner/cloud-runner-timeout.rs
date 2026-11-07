use std::time::Duration;

use aws_durable_execution_sdk as durable;
use durable::test_util::{CloudRunner, ExecutionStatus};

#[tokio::test]
async fn runs_against_a_deployed_function() -> Result<(), durable::BoxError> {
    let runner = CloudRunner::new("MyFunction")
        .qualifier("$LATEST")
        .region("us-east-1")
        .poll_interval(Duration::from_secs(2));

    let result = runner
        .run::<_, String>(serde_json::json!({ "name": "world" }))
        .await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);
    Ok(())
}
