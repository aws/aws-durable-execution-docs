use aws_durable_execution_sdk as durable;
use durable::test_util::{ExecutionStatus, LocalRunner};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
struct Event {
    fail: bool,
}

async fn handler(event: Event, _ctx: durable::DurableContext) -> Result<String, durable::BoxError> {
    if event.fail {
        return Err("intentional failure".into());
    }
    Ok("ok".to_owned())
}

#[tokio::test]
async fn reports_a_failed_execution() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, Event { fail: true }).await?;

    assert_eq!(result.status(), ExecutionStatus::Failed);
    let message = result.error_message().ok_or("no error message")?;
    assert!(message.contains("intentional failure"));
    Ok(())
}
