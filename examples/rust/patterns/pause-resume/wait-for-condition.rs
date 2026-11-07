use std::time::Duration;

use aws_durable_execution_sdk as durable;
use durable::builders::wait_for_condition::WaitDecision;
use serde::{Deserialize, Serialize};

#[derive(Deserialize)]
#[serde(rename_all = "camelCase")]
struct JobEvent {
    job_id: String,
}

#[derive(Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct JobState {
    job_id: String,
    status: String,
}

fn job_service_get_status(step_ctx: &durable::StepContext, _job_id: &str) -> String {
    let status = if step_ctx.attempt() < 3 {
        "running"
    } else {
        "completed"
    };
    status.to_owned()
}

async fn handler(
    event: JobEvent,
    ctx: durable::DurableContext,
) -> Result<JobState, durable::BoxError> {
    let final_state = ctx
        .wait_for_condition(
            |step_ctx, state: JobState| async move {
                let status = job_service_get_status(&step_ctx, &state.job_id);
                Ok(JobState { status, ..state })
            },
            JobState {
                job_id: event.job_id,
                status: "pending".to_owned(),
            },
        )
        .name("wait-for-job")
        .wait_strategy_fn(|state: JobState, attempt| {
            if state.status == "completed" {
                return WaitDecision::complete();
            }
            let delay_seconds = 2_u64.saturating_pow(attempt).min(60);
            WaitDecision::continue_with(Duration::from_secs(delay_seconds))
        })
        .await?;
    Ok(final_state)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
