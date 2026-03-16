from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import (
    CompletionConfig,
    ParallelConfig,
)

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    """Configure parallel execution."""
    
    # Configure to complete when first branch succeeds
    config = ParallelConfig(
        max_concurrency=3,  # Run at most 3 branches concurrently
        completion_config=CompletionConfig.first_successful(),
    )
    
    functions = [
        lambda ctx: ctx.step(lambda _: "Task 1", name="task1"),
        lambda ctx: ctx.step(lambda _: "Task 2", name="task2"),
        lambda ctx: ctx.step(lambda _: "Task 3", name="task3"),
    ]
    
    result: BatchResult[str] = context.parallel(functions, config=config)
    
    # Get the first successful result
    results = result.succeeded()
    first_result = results[0] if results else "None"
    
    return f"First successful result: {first_result}"
