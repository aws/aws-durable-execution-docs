use aws_durable_execution_sdk as durable;
use durable::OperationKind;
use durable::test_util::{ExecutionStatus, LocalRunner};

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    Ok(ctx
        .run_in_child_context(|child| async move {
            let a = child
                .step(|_| async { Ok("result-a".to_owned()) })
                .name("step-a")
                .await?;
            let b = child
                .step(|_| async { Ok("result-b".to_owned()) })
                .name("step-b")
                .await?;
            Ok(format!("{a}:{b}"))
        })
        .name("process")
        .await?)
}

#[tokio::test]
async fn executes_steps_inside_a_child_context() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, serde_json::json!({})).await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);
    assert_eq!(
        result.output().map(String::as_str),
        Some("result-a:result-b")
    );

    let ctx = result.operation("process").ok_or("process not found")?;
    assert_eq!(ctx.kind(), OperationKind::Context);

    let children: Vec<_> = result.children(ctx).collect();
    assert_eq!(children.len(), 2);
    let names: Vec<_> = children.iter().filter_map(|op| op.name()).collect();
    assert_eq!(names, ["step-a", "step-b"]);
    Ok(())
}
