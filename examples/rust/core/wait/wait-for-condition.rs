use std::time::Duration;

use aws_durable_execution_sdk as durable;
use durable::builders::wait_for_condition::WaitDecision;
use serde::{Deserialize, Serialize};

#[derive(Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct JobState {
    job_id: String,
    status: String,
    done: bool,
}

fn get_job_status(step_ctx: &durable::StepContext, _job_id: &str) -> String {
    let status = if step_ctx.attempt() < 2 {
        "RUNNING"
    } else {
        "COMPLETED"
    };
    status.to_owned()
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<JobState, durable::BoxError> {
    let result = ctx
        .wait_for_condition(
            |step_ctx, state: JobState| async move {
                let status = get_job_status(&step_ctx, &state.job_id);
                let done = status == "COMPLETED";
                Ok(JobState {
                    status,
                    done,
                    ..state
                })
            },
            JobState {
                job_id: "job-123".to_owned(),
                status: "pending".to_owned(),
                done: false,
            },
        )
        .name("wait_for_job")
        .wait_strategy_fn(|state: JobState, attempt| {
            if state.done {
                return WaitDecision::complete();
            }
            WaitDecision::continue_with(Duration::from_secs(
                5_u64.saturating_mul(2_u64.saturating_pow(attempt)).min(300),
            ))
        })
        .await?;
    Ok(result)
}

#[tokio::main]
async fn main() -> Result<(), lambda_runtime::Error> {
    durable::init_default_subscriber()?;
    durable::run(handler).await
}
