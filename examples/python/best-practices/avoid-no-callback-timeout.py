@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    callback = context.create_callback(name="approval")
    result = callback.result()
    return {"approved": result["approved"]}  # Crashes if timeout!
