from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import CallbackConfig

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    callback_config = CallbackConfig(
        timeout_seconds=120,
        heartbeat_timeout_seconds=60
    )
    
    callback = context.create_callback(
        name="example_callback",
        config=callback_config
    )
    
    return f"Callback created with ID: {callback.callback_id}"
