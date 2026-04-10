from aws_durable_execution_sdk_python.config import CallbackConfig
from aws_durable_execution_sdk_python.context import DurableContext
from aws_durable_execution_sdk_python.exceptions import CallbackError
from aws_durable_execution_sdk_python.execution import durable_execution


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    try:
        callback = context.create_callback(
            config=CallbackConfig(timeout_seconds=3600),
            name="approval",
        )
        context.wait_for_callback(callback)
        return {"status": "approved"}
    except CallbackError as e:
        return {"error": "CallbackError", "callback_id": e.callback_id}
