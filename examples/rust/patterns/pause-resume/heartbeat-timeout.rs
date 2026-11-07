use std::time::Duration;

use aws_durable_execution_sdk as durable;
use serde::Deserialize;
use serde_json::Value;

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct JobEvent {
    job_id: String,
}

fn start_job(_job_id: &str, _callback_id: &str) {}

async fn handler(
    event: JobEvent,
    ctx: durable::DurableContext,
) -> Result<Value, durable::BoxError> {
    let job_id = event.job_id;
    let outcome = ctx
        .wait_for_callback::<Value, _, _>(move |_step_ctx, callback_id| async move {
            start_job(&job_id, &callback_id);
            Ok(())
        })
        .name("long-running-job")
        .timeout(Duration::from_hours(24))
        .heartbeat(Duration::from_mins(10))
        .await?;
    Ok(outcome)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
