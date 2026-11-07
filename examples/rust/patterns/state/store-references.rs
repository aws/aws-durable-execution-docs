use aws_durable_execution_sdk as durable;
use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
struct DocumentEvent {
    key: String,
}

#[derive(Clone, Serialize, Deserialize)]
struct ObjectRef {
    bucket: String,
    key: String,
}

fn s3_get_object(_bucket: &str, key: &str) -> String {
    format!("contents-of-{key}")
}

fn stage_for_processing(data: &str) -> String {
    format!("staged-{data}")
}

fn summarize(data: &str) -> String {
    format!("summary-of-{data}")
}

async fn handler(
    event: DocumentEvent,
    ctx: durable::DurableContext,
) -> Result<(), durable::BoxError> {
    // Right: only the reference flows between steps.
    let key = event.key;
    let reference = ctx
        .step(move |_| async move {
            let data = s3_get_object("docs", &key);
            let staged_key = stage_for_processing(&data);
            Ok(ObjectRef {
                bucket: "processing".to_owned(),
                key: staged_key,
            })
        })
        .name("stage-document")
        .await?;

    ctx.step(move |_| async move {
        let data = s3_get_object(&reference.bucket, &reference.key);
        Ok(summarize(&data))
    })
    .name("summarize")
    .await?;
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
