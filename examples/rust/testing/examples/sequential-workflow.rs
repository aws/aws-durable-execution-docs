use aws_durable_execution_sdk as durable;
use durable::OperationKind;
use durable::test_util::{ExecutionStatus, LocalRunner};
use serde::{Deserialize, Serialize};

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct OrderEvent {
    order_id: String,
}

#[derive(Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct OrderRecord {
    order_id: String,
    status: String,
    #[serde(default, skip_serializing_if = "Option::is_none")]
    payment: Option<String>,
    #[serde(default, skip_serializing_if = "Option::is_none")]
    fulfillment: Option<String>,
}

async fn handler(
    event: OrderEvent,
    ctx: durable::DurableContext,
) -> Result<OrderRecord, durable::BoxError> {
    let validated = ctx
        .step(move |_| async move {
            Ok(OrderRecord {
                order_id: event.order_id,
                status: "validated".to_owned(),
                payment: None,
                fulfillment: None,
            })
        })
        .name("validate")
        .await?;
    let paid = ctx
        .step(move |_| async move {
            Ok(OrderRecord {
                payment: Some("completed".to_owned()),
                ..validated
            })
        })
        .name("payment")
        .await?;
    let fulfilled = ctx
        .step(move |_| async move {
            Ok(OrderRecord {
                fulfillment: Some("shipped".to_owned()),
                ..paid
            })
        })
        .name("fulfillment")
        .await?;
    Ok(fulfilled)
}

#[tokio::test]
async fn executes_all_steps_in_order() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let event = OrderEvent {
        order_id: "order-123".to_owned(),
    };
    let result = runner.run(handler, event).await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);

    let ops: Vec<_> = result
        .operations()
        .iter()
        .filter(|op| op.kind() == OperationKind::Step)
        .collect();
    assert_eq!(ops.len(), 3);
    let names: Vec<_> = ops.iter().filter_map(|op| op.name()).collect();
    assert_eq!(names, ["validate", "payment", "fulfillment"]);
    Ok(())
}
