use aws_durable_execution_sdk as durable;
use durable::builders::map_parallel::CompletionConfig;
use serde::Serialize;

#[derive(Serialize)]
#[serde(untagged)]
enum ConfigResult {
    Invalid { error: String },
    Valid { status: String },
}

async fn handler(
    _event: serde_json::Value,
    _ctx: durable::DurableContext,
) -> Result<ConfigResult, durable::BoxError> {
    let completion = CompletionConfig::builder()
        .tolerated_failure_percentage(150.0)
        .build();
    if let Err(err) = completion {
        tracing::error!(message = %err, "Invalid SDK configuration");
        return Ok(ConfigResult::Invalid {
            error: "InvalidConfiguration".to_owned(),
        });
    }
    Ok(ConfigResult::Valid {
        status: "ok".to_owned(),
    })
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
