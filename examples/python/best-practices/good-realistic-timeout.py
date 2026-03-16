from aws_durable_execution_sdk_python.config import CallbackConfig, Duration

@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    # Expected 2 minutes + 1 minute buffer = 3 minutes
    callback = context.create_callback(
        name="approval",
        config=CallbackConfig(timeout=Duration.from_minutes(3)),
    )
    return {"callback_id": callback.callback_id}
