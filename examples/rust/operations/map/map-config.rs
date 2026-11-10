use aws_durable_execution_sdk as durable;
use durable::builders::map_parallel::{CompletionConfig, NestingMode};
use serde::Deserialize;

#[derive(Deserialize)]
struct UrlsEvent {
    urls: Vec<String>,
}

async fn handler(
    event: UrlsEvent,
    ctx: durable::DurableContext,
) -> Result<Vec<String>, durable::BoxError> {
    let results = ctx
        .map(event.urls, |ctx, url, index| async move {
            Ok(ctx
                .step(move |_| async move { Ok(fetch(&url)) })
                .name(format!("fetch-{index}"))
                .await?)
        })
        .name("fetch-urls")
        .max_concurrency(5)
        .completion(CompletionConfig::with_tolerated_failure_count(2))
        .nesting(NestingMode::Flat)
        .await?;

    Ok(results)
}

// A stand-in for an HTTP GET that returns the response body.
fn fetch(url: &str) -> String {
    format!("body of {url}")
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
