use aws_durable_execution_sdk as durable;
use serde::Deserialize;

#[derive(Clone, Deserialize)]
struct Order {
    id: String,
}

#[derive(Clone, Deserialize)]
struct Item {
    id: String,
}

#[derive(Deserialize)]
struct OrderEvent {
    order: Order,
    item: Item,
}

fn validate_order(order: &Order) -> bool {
    !order.id.is_empty()
}

fn save_item(item: &Item) -> String {
    format!("saved-{}", item.id)
}

async fn handler(event: OrderEvent, ctx: durable::DurableContext) -> Result<(), durable::BoxError> {
    let OrderEvent { order, item } = event;

    // Stable, descriptive name.
    ctx.step(move |_| async move { Ok(validate_order(&order)) })
        .name("validate-order")
        .await?;

    // Dynamic but deterministic: include the item ID from the input.
    let name = format!("save-item-{}", item.id);
    ctx.step(move |_| async move { Ok(save_item(&item)) })
        .name(name)
        .await?;
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
