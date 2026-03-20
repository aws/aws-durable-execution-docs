from aws_durable_execution_sdk_python.waits import WaitForConditionConfig, WaitForConditionDecision
from aws_durable_execution_sdk_python.config import Duration


def check_status(state, check_context):
    status = get_job_status(state["job_id"])
    return {"job_id": state["job_id"], "status": status}


def wait_strategy(state, attempt):
    if state["status"] == "completed":
        return WaitForConditionDecision.stop_polling()
    return WaitForConditionDecision.continue_waiting(Duration.from_minutes(1))


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    job_id = context.step(lambda ctx: start_job())

    result = context.wait_for_condition(
        check=check_status,
        config=WaitForConditionConfig(
            wait_strategy=wait_strategy,
            initial_state={"job_id": job_id},
        )
    )
    return result
