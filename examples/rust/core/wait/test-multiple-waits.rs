use std::time::Duration;

use aws_durable_execution_sdk as durable;
use durable::test_util::{LocalRunner, TestOperationDetails};
use durable::{OperationKind, OperationStatus};
use serde::{Deserialize, Serialize};

#[derive(Debug, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct WaitResult {
    completed_waits: u32,
    final_step: String,
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<WaitResult, durable::BoxError> {
    ctx.wait(Duration::from_secs(5)).name("wait-1").await?;
    ctx.wait(Duration::from_secs(5)).name("wait-2").await?;
    Ok(WaitResult {
        completed_waits: 2,
        final_step: "done".to_owned(),
    })
}

#[tokio::test]
async fn should_handle_multiple_sequential_wait_operations() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, serde_json::json!({})).await?;

    let expected = WaitResult {
        completed_waits: 2,
        final_step: "done".to_owned(),
    };
    assert_eq!(result.output(), Some(&expected));

    assert_eq!(result.operations().len(), 2);

    for name in ["wait-1", "wait-2"] {
        let wait = result.operation(name).ok_or("wait not found")?;
        assert_eq!(wait.kind(), OperationKind::Wait);
        assert_eq!(wait.status(), OperationStatus::Succeeded);
        let TestOperationDetails::Wait(details) = wait.details() else {
            return Err("not a wait".into());
        };
        assert_eq!(details.duration(), Some(Duration::from_secs(5)));
    }

    Ok(())
}
