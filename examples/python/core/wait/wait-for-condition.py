from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.waits import WaitForConditionConfig, WaitForConditionDecision
from aws_durable_execution_sdk_python.config import Duration


def check_job_status(state, check_context):
    status = get_job_status(state["job_id"])
    return {
        "job_id": state["job_id"],
        "status": status,
        "done": status == "COMPLETED"
    }


def wait_strategy(state, attempt):
    if state["done"]:
        return WaitForConditionDecision.stop_polling()
    delay = min(5 * (2 ** attempt), 300)
    return WaitForConditionDecision.continue_waiting(Duration.from_seconds(delay))


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    result = context.wait_for_condition(
        check=check_job_status,
        config=WaitForConditionConfig(
            wait_strategy=wait_strategy,
            initial_state={"job_id": "job-123", "done": False},
        )
    )
    return result
