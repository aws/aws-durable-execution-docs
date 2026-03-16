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
    """Handle individual branch failures."""
    
    def successful_task(ctx: DurableContext) -> str:
        return ctx.step(lambda _: "Success")
    
    def failing_task(ctx: DurableContext) -> str:
        return ctx.step(lambda _: raise_error("Task failed"))
    
    functions = [successful_task, failing_task, successful_task]
    
    # Use all_completed to collect per-branch status; check started_count for early completion
    config = ParallelConfig(
        completion_config=CompletionConfig.all_completed()
    )
    
    result: BatchResult[str] = context.parallel(functions, config=config)
    
    return {
        "successful": result.succeeded(),
        "failed_count": result.failure_count,
        "status": result.status.value,
    }
