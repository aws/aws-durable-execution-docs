use aws_config::retry::RetryConfig;
use aws_config::{BehaviorVersion, Region};
use aws_durable_execution_sdk as durable;
use serde::Serialize;

#[derive(Serialize)]
struct Response {
    status: String,
}

async fn handler(
    _event: serde_json::Value,
    _ctx: durable::DurableContext,
) -> Result<Response, durable::BoxError> {
    Ok(Response {
        status: "success".to_owned(),
    })
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;

    let config = aws_config::defaults(BehaviorVersion::latest())
        .region(Region::new("us-west-2"))
        .retry_config(RetryConfig::adaptive().with_max_attempts(5))
        .load()
        .await;
    let custom_client = durable::LambdaClient::new(&config);

    let options = durable::Options::builder()
        .lambda_client(custom_client)
        .build()?;

    durable::run_with_options(handler, options).await
}
