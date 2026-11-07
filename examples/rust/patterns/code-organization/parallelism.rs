use aws_durable_execution_sdk as durable;
use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
struct EnrichEvent {
    items: Vec<String>,
}

#[derive(Serialize)]
struct EnrichResult {
    fx: String,
    weather: String,
    quote: String,
    results: Vec<String>,
}

fn fx_rates_latest() -> String {
    "1.10".to_owned()
}

fn weather_api_get() -> String {
    "sunny".to_owned()
}

fn quote_api_get() -> String {
    "42".to_owned()
}

fn process(item: &str) -> String {
    format!("processed-{item}")
}

async fn handler(
    event: EnrichEvent,
    ctx: durable::DurableContext,
) -> Result<EnrichResult, durable::BoxError> {
    // A small number of fixed branches.
    let enriched = ctx
        .parallel([
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok(fx_rates_latest()) })
                    .name("fx")
                    .await?)
            }),
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok(weather_api_get()) })
                    .name("weather")
                    .await?)
            }),
            durable::Branch::unnamed(|ctx| async move {
                Ok(ctx
                    .step(|_| async { Ok(quote_api_get()) })
                    .name("quote")
                    .await?)
            }),
        ])
        .name("enrich")
        .await?;
    let Ok::<[String; 3], _>([fx, weather, quote]) = enriched.try_into() else {
        return Err("enrich returned an unexpected number of results".into());
    };

    // A variable number of items.
    let results = ctx
        .map(event.items, |ctx, item: String, _index| async move {
            Ok(ctx
                .step(move |_| async move { Ok(process(&item)) })
                .name("process")
                .await?)
        })
        .max_concurrency(10)
        .await?;

    Ok(EnrichResult {
        fx,
        weather,
        quote,
        results,
    })
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
