@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Handle callback results conditionally."""
    callback = context.create_callback(
        name="conditional_callback",
        config=CallbackConfig(timeout=Duration.from_minutes(10)),
    )
    
    # Send request
    send_request(callback.callback_id, event["request_type"])
    
    # Wait for result
    result = callback.result()
    
    # Handle different result types
    if result is None:
        return {"status": "timeout", "message": "No response received"}
    
    result_type = result.get("type")
    
    if result_type == "success":
        return process_success(result)
    elif result_type == "partial":
        return process_partial(result)
    else:
        return process_failure(result)
