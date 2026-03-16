from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import CallbackConfig, Duration

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    # Configure callback with custom timeouts
    config = CallbackConfig(
        timeout=Duration.from_seconds(60),
        heartbeat_timeout=Duration.from_seconds(30),
    )
    
    callback = context.create_callback(
        name="timeout_callback",
        config=config,
    )
    
    return f"Callback created with 60s timeout: {callback.callback_id}"
