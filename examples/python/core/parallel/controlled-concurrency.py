from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import ParallelConfig

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Process many items with controlled concurrency."""
    items = event.get("items", [])
    
    # Create a function for each item
    functions = [
        lambda ctx, item=item: ctx.step(
            lambda _: f"Processed {item}",
            name=f"process_{item}"
        )
        for item in items
    ]
    
    # Process at most 10 items concurrently
    config = ParallelConfig(max_concurrency=10)
    
    result: BatchResult[str] = context.parallel(functions, config=config)
    
    return {
        "processed": result.success_count,
        "failed": result.failure_count,
        "results": result.get_results(),
    }
