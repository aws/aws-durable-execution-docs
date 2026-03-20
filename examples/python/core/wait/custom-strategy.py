from aws_durable_execution_sdk_python.waits import WaitForConditionDecision
from aws_durable_execution_sdk_python.config import Duration


def strategy(state, attempt):
    if state["status"] == "COMPLETED":
        return WaitForConditionDecision.stop_polling()
    if attempt >= 10:
        raise ValueError("Max attempts exceeded")
    return WaitForConditionDecision.continue_waiting(Duration.from_seconds(attempt * 5))
