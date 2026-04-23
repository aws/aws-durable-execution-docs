from aws_durable_execution_sdk_python.config import Duration
from aws_durable_execution_sdk_python.waits import WaitForConditionConfig


def check(state, ctx):
    state["status"] = job_service.get_status(state["jobId"])
    return state


def wait_strategy(state, attempt):
    if state["status"] == "completed":
        return {"should_continue": False}
    delay = min(2 ** attempt, 60)
    return {"should_continue": True, "delay": Duration.from_seconds(delay)}


final_state = context.wait_for_condition(
    check,
    WaitForConditionConfig(
        initial_state={"jobId": event["jobId"], "status": "pending"},
        wait_strategy=wait_strategy,
    ),
    name="wait-for-job",
)
