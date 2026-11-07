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

fn strategy() -> impl Fn(JobState, u32) -> WaitDecision {
    |state, attempt| {
        if state.status == "COMPLETED" {
            return WaitDecision::complete();
        }
        if attempt >= 10 {
            return WaitDecision::exhausted("Max attempts exceeded");
        }
        WaitDecision::continue_with(Duration::from_secs(u64::from(attempt) * 5))
    }
}
