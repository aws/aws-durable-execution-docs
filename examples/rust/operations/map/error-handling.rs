use aws_durable_execution_sdk as durable;
use serde::Deserialize;

#[derive(Deserialize)]
struct ItemsEvent {
    items: Vec<String>,
}

async fn handler(event: ItemsEvent, ctx: durable::DurableContext) -> Result<(), durable::BoxError> {
    let result = ctx
        .map(event.items, |ctx, item, index| async move {
            Ok(ctx
                .step(move |_| async move {
                    if item == "bad" {
                        return Err("bad item".into());
                    }
                    Ok(item.to_uppercase())
                })
                .name(format!("process-{index}"))
                .await?)
        })
        .name("process-items")
        .await_batch()
        .await?;

    if result.has_failure() {
        let errors: Vec<_> = result.errors().collect();
        tracing::info!("{} items failed: {errors:?}", result.failure_count());
    }

    let successes: Vec<_> = result.results().collect();
    tracing::info!("{} items succeeded: {successes:?}", result.success_count());
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
