@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Use fallback when callback times out."""
    callback = context.create_callback(
        name="primary_service",
        config=CallbackConfig(timeout=Duration.from_seconds(30)),
    )
    
    # Try primary service
    send_to_primary_service(callback.callback_id, event["data"])
    
    result = callback.result()
    
    if result is None:
        # Primary service timed out, use fallback
        fallback_callback = context.create_callback(
            name="fallback_service",
            config=CallbackConfig(timeout=Duration.from_minutes(2)),
        )
        
        send_to_fallback_service(fallback_callback.callback_id, event["data"])
        result = fallback_callback.result()
    
    return {"result": result, "source": "primary" if result else "fallback"}
