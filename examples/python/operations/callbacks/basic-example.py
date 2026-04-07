from typing import Any

from aws_durable_execution_sdk_python import DurableContext, durable_execution


@durable_execution
def handler(event: Any, context: DurableContext) -> dict:
    callback = context.create_callback(name="wait-for-approval")

    # Send callback.callback_id to the external system that will resume this function.
    send_approval_request(callback.callback_id, event["request_id"])

    # Execution suspends here until the external system calls back.
    result = callback.result()
    return {"approved": True, "result": result}
