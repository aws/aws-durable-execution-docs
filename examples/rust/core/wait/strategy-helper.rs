use std::time::Duration;

use aws_durable_execution_sdk as durable;
use durable::builders::JitterStrategy;
use durable::builders::wait_for_condition::WaitStrategy;
use serde::{Deserialize, Serialize};

#[derive(Clone, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
struct JobState {
    job_id: String,
    status: String,
    done: bool,
}

fn strategy() -> WaitStrategy<JobState> {
    WaitStrategy::builder(|state: &JobState| state.status == "COMPLETED")
        .max_attempts(10)
        .initial_delay(Duration::from_secs(5))
        .max_delay(Duration::from_mins(5))
        .backoff_rate(2.0)
        .jitter(JitterStrategy::Full)
        .build()
}
