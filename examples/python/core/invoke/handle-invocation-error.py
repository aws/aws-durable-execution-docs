from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    CallableRuntimeError,
)

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Handle errors from invoked functions."""
    try:
        result = context.invoke(
            function_name="risky-function",
            payload=event,
            name="risky_operation",
        )
        return {"status": "success", "result": result}
    
    except CallableRuntimeError as e:
        # Handle the error from the invoked function
        context.logger.error(f"Invoked function failed: {e}")
        return {
            "status": "failed",
            "error": str(e),
        }
