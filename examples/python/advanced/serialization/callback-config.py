from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import CallbackConfig, Duration

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    config = CallbackConfig(
        timeout=Duration.from_hours(2),
        serdes=CustomSerDes()
    )
    callback = context.create_callback(config=config)
    
    # Send callback.callback_id to external system
    return {"callback_id": callback.callback_id}
