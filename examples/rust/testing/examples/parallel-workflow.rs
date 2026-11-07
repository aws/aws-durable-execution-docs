use aws_durable_execution_sdk as durable;
use durable::test_util::{ExecutionStatus, LocalRunner};

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<Vec<String>, durable::BoxError> {
    let results = ctx
        .parallel([
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("data-a".to_owned()) })
                    .name("fetch-a")
                    .await?)
            }),
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("data-b".to_owned()) })
                    .name("fetch-b")
                    .await?)
            }),
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok("data-c".to_owned()) })
                    .name("fetch-c")
                    .await?)
            }),
        ])
        .name("fetch-all")
        .await?;
    Ok(results)
}

#[tokio::test]
async fn executes_branches_in_parallel_and_collects_results() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, serde_json::json!({})).await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);
    let expected = ["data-a", "data-b", "data-c"].map(String::from).to_vec();
    assert_eq!(result.output(), Some(&expected));
    Ok(())
}
