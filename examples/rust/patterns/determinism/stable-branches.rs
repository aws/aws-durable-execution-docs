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
    // Right: the SDK checkpoints the decision.
    let shift = ctx
        .step(|_| async {
            let shift = if current_hour()? < 12 {
                "morning"
            } else {
                "afternoon"
            };
            Ok(shift.to_owned())
        })
        .name("pick-shift")
        .await?;
    if shift == "morning" {
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
