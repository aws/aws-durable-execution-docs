@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Retry failed invocations."""
    max_retries = 3
    
    for attempt in range(max_retries):
        try:
            result = context.invoke(
                function_name="unreliable-function",
                payload=event,
                name=f"attempt_{attempt + 1}",
            )
            return {"status": "success", "result": result, "attempts": attempt + 1}
        
        except CallableRuntimeError as e:
            if attempt == max_retries - 1:
                # Last attempt failed
                return {
                    "status": "failed",
                    "error": str(e),
                    "attempts": max_retries,
                }
            # Wait before retrying
            context.wait(Duration.from_seconds(2 ** attempt))
    
    return {"status": "failed", "reason": "max_retries_exceeded"}
