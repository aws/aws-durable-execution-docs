from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Demonstrate result collection."""
    
    functions = [
        lambda ctx: ctx.step(lambda _: f"Result {i}")
        for i in range(5)
    ]
    
    result: BatchResult[str] = context.parallel(functions)
    
    return {
        # Successful results only
        "successful": result.succeeded(),
        
        # Failed results (if any)
        "failed": result.failed(),
        
        # Counts
        "total_count": result.total_count,
        "success_count": result.success_count,
        "failure_count": result.failure_count,
        "started_count": result.started_count,
        
        # Status information
        "status": result.status.value,
        "completion_reason": result.completion_reason.value,
    }
