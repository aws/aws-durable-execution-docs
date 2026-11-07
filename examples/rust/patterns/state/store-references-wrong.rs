use aws_durable_execution_sdk as durable;
use serde::Deserialize;

#[derive(Deserialize)]
struct DocumentEvent {
    key: String,
}

fn s3_get_object(_bucket: &str, key: &str) -> String {
    format!("contents-of-{key}")
}

fn summarize(data: &str) -> String {
    format!("summary-of-{data}")
}

async fn handler(
    event: DocumentEvent,
    ctx: durable::DurableContext,
) -> Result<(), durable::BoxError> {
    // Wrong: the step returns and checkpoints the full document.
    let key = event.key;
    let document = ctx
        .step(move |_| async move { Ok(s3_get_object("docs", &key)) })
        .name("fetch-document")
        .await?;
    ctx.step(move |_| async move { Ok(summarize(&document)) })
        .name("summarize")
        .await?;
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
