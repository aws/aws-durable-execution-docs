@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    # Too short - will timeout before external system responds
    callback = context.create_callback(
        name="approval",
        config=CallbackConfig(timeout=Duration.from_seconds(5)),
    )
    return {"callback_id": callback.callback_id}
