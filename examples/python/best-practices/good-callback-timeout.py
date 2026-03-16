@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    callback = context.create_callback(name="approval")
    result = callback.result()
    
    if result is None:
        return {"status": "timeout", "approved": False}
    
    return {"status": "completed", "approved": result.get("approved", False)}
