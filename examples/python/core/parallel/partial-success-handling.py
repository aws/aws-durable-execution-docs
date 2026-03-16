from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import (
    CompletionConfig,
    ParallelConfig,
)

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Allow some branches to fail."""
    
    # Require at least 2 successes, tolerate up to 1 failure
    config = ParallelConfig(
        completion_config=CompletionConfig(
            min_successful=2,
            tolerated_failure_count=1,
        )
    )
    
    functions = [
        lambda ctx: ctx.step(lambda _: "Success 1"),
        lambda ctx: ctx.step(lambda _: "Success 2"),
        lambda ctx: ctx.step(lambda _: raise_error()),  # This might fail
    ]
    
    result: BatchResult[str] = context.parallel(functions, config=config)
    
    return {
        "status": "partial_success",
        "successful": result.get_results(),
        "failed_count": result.failure_count,
    }
