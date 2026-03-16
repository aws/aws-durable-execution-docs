from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)

@durable_execution
def handler(event: dict, context: DurableContext) -> list[str]:
    """Execute three tasks in parallel."""
    # Define functions to execute in parallel
    task1 = lambda ctx: ctx.step(lambda _: "Task 1 complete", name="task1")
    task2 = lambda ctx: ctx.step(lambda _: "Task 2 complete", name="task2")
    task3 = lambda ctx: ctx.step(lambda _: "Task 3 complete", name="task3")
    
    # Execute all tasks concurrently
    result: BatchResult[str] = context.parallel([task1, task2, task3])
    
    # Return successful results
    return result.get_results()
