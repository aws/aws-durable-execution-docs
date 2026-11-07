use aws_durable_execution_sdk as durable;
use durable::plugin::{HookWork, InvocationInfo, OperationInfo, Plugin, PluginFactory};

struct ExamplePlugin;

impl Plugin for ExamplePlugin {
    fn on_operation_start(&self, info: &OperationInfo) -> HookWork {
        tracing::info!("operation {} start", info.name().unwrap_or_default());
        Ok(None)
    }
}

struct ExamplePluginFactory;

impl PluginFactory for ExamplePluginFactory {
    fn create_plugin(
        &self,
        _info: &InvocationInfo,
    ) -> Result<Option<Box<dyn Plugin>>, durable::BoxError> {
        Ok(Some(Box::new(ExamplePlugin)))
    }
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<String, durable::BoxError> {
    let result = ctx
        .step(|_| async { Ok("done".to_owned()) })
        .name("process")
        .await?;
    Ok(result)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    let options = durable::Options::builder()
        .plugin(ExamplePluginFactory)
        .build()?;
    durable::run_with_options(handler, options).await
}
