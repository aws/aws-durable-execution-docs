use aws_durable_execution_sdk as durable;
use durable::test_util::{ExecutionStatus, LocalRunner};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
struct Event {
    premium: bool,
}

async fn handler(event: Event, ctx: durable::DurableContext) -> Result<String, durable::BoxError> {
    if event.premium {
        return Ok(ctx
            .step(|_| async { Ok("premium".to_owned()) })
            .name("premium-path")
            .await?);
    }
    Ok(ctx
        .step(|_| async { Ok("standard".to_owned()) })
        .name("standard-path")
        .await?)
}

#[tokio::test]
async fn takes_the_premium_path() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, Event { premium: true }).await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);
    assert_eq!(result.output().map(String::as_str), Some("premium"));
    Ok(())
}

#[tokio::test]
async fn takes_the_standard_path() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, Event { premium: false }).await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);
    assert_eq!(result.output().map(String::as_str), Some("standard"));
    Ok(())
}
