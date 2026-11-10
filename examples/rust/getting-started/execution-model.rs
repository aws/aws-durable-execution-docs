use std::time::Duration;

use aws_durable_execution_sdk as durable;
use serde::Deserialize;

#[derive(Deserialize)]
struct Event {
    id: String,
}

async fn handler(event: Event, ctx: durable::DurableContext) -> Result<String, durable::BoxError> {
    // The SDK checkpoints the result.
    let data = ctx
        .step(move |_| async move { Ok(fetch_data(&event.id)) })
        .name("fetch-data")
        .await?;

    // The wait consumes no compute time.
    ctx.wait(Duration::from_secs(30)).await?;

    let result = ctx
        .step(move |_| async move { Ok(process_data(&data)) })
        .name("process-data")
        .await?;

    Ok(result)
}

fn fetch_data(id: &str) -> String {
    format!("data-for-{id}")
}

fn process_data(data: &str) -> String {
    format!("processed-{data}")
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
