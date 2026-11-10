use aws_durable_execution_sdk as durable;
use lambda_runtime::tracing::subscriber;
use subscriber::Layer as _;
use subscriber::layer::SubscriberExt as _;
use subscriber::util::SubscriberInitExt as _;

async fn handler(
    _event: serde_json::Value,
    _ctx: durable::DurableContext,
) -> Result<(), durable::BoxError> {
    tracing::info!("Using a custom subscriber");
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    subscriber::registry()
        .with(
            subscriber::fmt::layer()
                .json()
                .with_filter(durable::ReplayFilterLayer::new()),
        )
        .try_init()?;
    durable::run(handler).await
}
