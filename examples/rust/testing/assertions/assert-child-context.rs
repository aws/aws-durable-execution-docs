use aws_durable_execution_sdk as durable;
use durable::OperationKind;
use durable::test_util::{ExecutionStatus, LocalRunner};

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<i32, durable::BoxError> {
    Ok(ctx
        .run_in_child_context(|child| async move {
            Ok(child.step(|_| async { Ok(42) }).name("compute").await?)
        })
        .name("process")
        .await?)
}

#[tokio::test]
async fn asserts_on_child_context_and_its_operations() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, serde_json::json!({})).await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);

    let ctx = result.operation("process").ok_or("process not found")?;
    assert_eq!(ctx.kind(), OperationKind::Context);
    let value: i32 = serde_json::from_str(ctx.result().ok_or("no context result")?)?;
    assert_eq!(value, 42);

    let children: Vec<_> = result.children(ctx).collect();
    assert_eq!(children.len(), 1);
    assert_eq!(children.first().and_then(|op| op.name()), Some("compute"));
    Ok(())
}
