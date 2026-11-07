use std::time::{SystemTime, UNIX_EPOCH};

use aws_durable_execution_sdk as durable;

fn do_thing() -> String {
    "done".to_owned()
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<(), durable::BoxError> {
    // Wrong: non-deterministic name, changes on replay.
    let now = SystemTime::now().duration_since(UNIX_EPOCH)?.as_millis();
    ctx.step(|_| async { Ok(do_thing()) })
        .name(format!("run-{now}"))
        .await?;
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
