from typing import Any

from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.types import WaitForCallbackContext


@durable_execution
def handler(event: Any, context: DurableContext) -> dict:
    def submit(callback_id: str, ctx: WaitForCallbackContext) -> None:
        send_approval_request(callback_id, event["request_id"])

    result = context.wait_for_callback(submitter=submit, name="wait-for-approval")
    return {"approved": True, "result": result}
