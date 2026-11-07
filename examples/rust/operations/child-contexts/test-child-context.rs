use aws_durable_execution_sdk as durable;
use durable::test_util::LocalRunner;
use durable::{OperationKind, OperationStatus};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct OrderEvent {
    order_id: String,
}

#[derive(Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct ValidatedOrder {
    order_id: String,
    valid: bool,
}

#[derive(Debug, PartialEq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct ChargedOrder {
    order_id: String,
    valid: bool,
    charged: bool,
}

async fn handler(
    event: OrderEvent,
    ctx: durable::DurableContext,
) -> Result<ChargedOrder, durable::BoxError> {
    let order_id = event.order_id;
    let result = ctx
        .run_in_child_context(|child| async move {
            let validated = child
                .step(move |_| async move { Ok(validate(order_id)) })
                .name("validate")
                .await?;
            let charged = child
                .step(move |_| async move { Ok(charge(validated)) })
                .name("charge")
                .await?;
            Ok(charged)
        })
        .name("process-order")
        .await?;
    Ok(result)
}

fn validate(order_id: String) -> ValidatedOrder {
    ValidatedOrder {
        order_id,
        valid: true,
    }
}

fn charge(order: ValidatedOrder) -> ChargedOrder {
    ChargedOrder {
        order_id: order.order_id,
        valid: order.valid,
        charged: true,
    }
}

fn order_event() -> OrderEvent {
    OrderEvent {
        order_id: "order-1".to_owned(),
    }
}

#[tokio::test]
async fn returns_the_child_context_result() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, order_event()).await?;
    let expected = ChargedOrder {
        order_id: "order-1".to_owned(),
        charged: true,
        valid: true,
    };
    assert_eq!(result.output(), Some(&expected));
    Ok(())
}

#[tokio::test]
async fn records_a_context_operation() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, order_event()).await?;
    let context_op = result
        .operation("process-order")
        .ok_or("no process-order operation")?;
    assert_eq!(context_op.kind(), OperationKind::Context);
    assert_eq!(context_op.status(), OperationStatus::Succeeded);
    Ok(())
}

#[tokio::test]
async fn child_operations_are_nested_under_the_context_operation() -> Result<(), durable::BoxError>
{
    let runner = LocalRunner::new();
    let result = runner.run(handler, order_event()).await?;
    let context_op = result
        .operation("process-order")
        .ok_or("no process-order operation")?;
    assert_eq!(result.children(context_op).count(), 2);
    Ok(())
}
