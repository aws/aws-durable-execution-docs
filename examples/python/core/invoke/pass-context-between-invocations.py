@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Pass context between invocations."""
    # First invocation creates context
    initial_context = context.invoke(
        function_name="initialize-context",
        payload=event,
        name="initialize",
    )
    
    # Second invocation uses the context
    processed = context.invoke(
        function_name="process-with-context",
        payload={
            "data": event["data"],
            "context": initial_context,
        },
        name="process",
    )
    
    # Third invocation finalizes
    final_result = context.invoke(
        function_name="finalize",
        payload={
            "processed": processed,
            "context": initial_context,
        },
        name="finalize",
    )
    
    return final_result
