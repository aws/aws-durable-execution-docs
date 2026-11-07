use std::time::Duration;

use aws_durable_execution_sdk as durable;
use durable::builders::wait_for_condition::WaitDecision;
use durable::test_util::{ExecutionStatus, LocalRunner};
use serde::{Deserialize, Serialize};

#[derive(Clone, Serialize, Deserialize)]
struct PollState {
    attempts: u32,
    done: bool,
}

async fn handler(
    _event: serde_json::Value,
    ctx: durable::DurableContext,
) -> Result<PollState, durable::BoxError> {
    Ok(ctx
        .wait_for_condition(
            |_step_ctx, state: PollState| async move {
                let next = PollState {
                    attempts: state.attempts + 1,
                    done: state.attempts >= 2,
                };
                Ok(next)
            },
            PollState {
                attempts: 0,
                done: false,
            },
        )
        .name("poll-job")
        .wait_strategy_fn(|state: PollState, _attempt| {
            if state.done {
                WaitDecision::complete()
            } else {
                WaitDecision::continue_with(Duration::from_secs(1))
            }
        })
        .await?)
}

#[tokio::test]
async fn polls_until_condition_is_met() -> Result<(), durable::BoxError> {
    let runner = LocalRunner::new();
    let result = runner.run(handler, serde_json::json!({})).await?;

    assert_eq!(result.status(), ExecutionStatus::Succeeded);
    assert_eq!(result.output().map(|state| state.done), Some(true));
    Ok(())
}
