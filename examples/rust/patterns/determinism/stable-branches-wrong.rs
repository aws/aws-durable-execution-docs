use std::time::{SystemTime, SystemTimeError, UNIX_EPOCH};

use aws_durable_execution_sdk as durable;

fn current_hour() -> Result<u64, SystemTimeError> {
    Ok(SystemTime::now().duration_since(UNIX_EPOCH)?.as_secs() / 3600 % 24)
}

fn run_morning() -> String {
    "morning-done".to_owned()
}

fn run_afternoon() -> String {
    "afternoon-done".to_owned()
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<(), durable::BoxError> {
    // Wrong: replay may see different values of `SystemTime::now()`.
    if current_hour()? < 12 {
        ctx.step(|_| async { Ok(run_morning()) })
            .name("morning-work")
            .await?;
    } else {
        ctx.step(|_| async { Ok(run_afternoon()) })
            .name("afternoon-work")
            .await?;
    }
    Ok(())
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
