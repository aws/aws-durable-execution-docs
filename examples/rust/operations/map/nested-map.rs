use aws_durable_execution_sdk as durable;
use serde::Deserialize;

#[derive(Deserialize)]
struct Region {
    name: String,
    items: Vec<String>,
}

#[derive(Deserialize)]
struct RegionsEvent {
    regions: Vec<Region>,
}

async fn handler(
    event: RegionsEvent,
    ctx: durable::DurableContext,
) -> Result<Vec<Vec<String>>, durable::BoxError> {
    let results = ctx
        .map(event.regions, |ctx, region, _index| async move {
            let inner = ctx
                .map(region.items, |inner_ctx, item, i| async move {
                    Ok(inner_ctx
                        .step(move |_| async move { Ok(item.to_uppercase()) })
                        .name(format!("item-{i}"))
                        .await?)
                })
                .name(format!("process-{}", region.name))
                .await?;
            Ok(inner)
        })
        .name("process-regions")
        .await?;

    Ok(results)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
