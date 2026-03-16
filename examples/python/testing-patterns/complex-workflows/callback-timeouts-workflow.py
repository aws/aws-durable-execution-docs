from aws_durable_execution_sdk_python.config import CallbackConfig

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    config = CallbackConfig(timeout_seconds=60, heartbeat_timeout_seconds=30)
    callback = context.create_callback(name="approval_callback", config=config)
    return f"Callback created: {callback.callback_id}"
