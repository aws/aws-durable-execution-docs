from aws_durable_execution_sdk_python import CallbackError
from aws_durable_execution_sdk_python.config import CallbackConfig

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Handle callback with error handling."""
    try:
        callback = context.create_callback(
            config=CallbackConfig(timeout_seconds=3600),
            name="approval"
        )
        context.wait_for_callback(callback)
        return {"status": "approved"}
    except CallbackError as e:
        return {"error": "CallbackError", "callback_id": e.callback_id}
