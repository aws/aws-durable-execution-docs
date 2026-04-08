@durable_with_child_context
def risky_operation(ctx: DurableContext, data: dict) -> dict:
    """Operation that might fail."""
    try:
        result = ctx.step(
            lambda _: potentially_failing_operation(data),
            name="risky_step"
        )
        return {"status": "success", "result": result}
    except Exception as e:
        # Handle error within child context
        fallback = ctx.step(
            lambda _: fallback_operation(data),
            name="fallback"
        )
        return {"status": "fallback", "result": fallback, "error": str(e)}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Handle errors in child context."""
    result = context.run_in_child_context(
        risky_operation(event["data"]),
        name="risky_operation"
    )
    
    if result["status"] == "fallback":
        # Log or handle fallback scenario
        return {"warning": "Used fallback", "result": result["result"]}
    
    return result
