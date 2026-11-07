use aws_durable_execution_sdk as durable;
use serde::Deserialize;

#[derive(Deserialize)]
struct BatchEvent {
    items: Vec<String>,
}

fn process_item(item: &str) -> String {
    format!("out-{item}")
}

fn store_output(output: &str) -> String {
    format!("s3://bucket/{output}")
}

async fn handler(
    event: BatchEvent,
    ctx: durable::DurableContext,
) -> Result<Vec<String>, durable::BoxError> {
    let results = ctx
        .map(event.items, |ctx, item: String, _index| async move {
            let output_key = ctx
                .step(move |_| async move {
                    let output = process_item(&item);
                    Ok(store_output(&output))
                })
                .name("process")
                .await?;
            Ok(output_key)
        })
        .await?;

    // `results` carries pointers, not payloads.
    Ok(results)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
