use std::time::{SystemTime, UNIX_EPOCH};

use aws_durable_execution_sdk as durable;
use durable::serdes::{Serdes, SerdesContext};
use serde::de::DeserializeOwned;
use serde::{Deserialize, Serialize};

struct StrictSerdes;

impl<T> Serdes<T> for StrictSerdes
where
    T: Serialize + DeserializeOwned + Send + 'static,
{
    fn serialize(
        &self,
        value: T,
        _context: SerdesContext,
    ) -> impl Future<Output = Result<String, durable::BoxError>> + Send {
        std::future::ready(serde_json::to_string(&value).map_err(Into::into))
    }

    fn deserialize(
        &self,
        data: String,
        _context: SerdesContext,
    ) -> impl Future<Output = Result<T, durable::BoxError>> + Send {
        std::future::ready(serde_json::from_str(&data).map_err(Into::into))
    }
}

#[derive(Serialize, Deserialize)]
struct BuildResult {
    message: String,
    timestamp: u64,
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<BuildResult, durable::BoxError> {
    let result = ctx
        .step(|_| async {
            let millis = SystemTime::now().duration_since(UNIX_EPOCH)?.as_millis();
            Ok(BuildResult {
                message: "hello".to_owned(),
                timestamp: u64::try_from(millis)?,
            })
        })
        .name("build-result")
        .serdes(StrictSerdes)
        .await?;
    Ok(result)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
