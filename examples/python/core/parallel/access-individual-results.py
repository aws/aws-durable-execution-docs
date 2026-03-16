from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Access individual results from parallel execution."""
    
    def task_a(ctx: DurableContext) -> str:
        return ctx.step(lambda _: "Result A")
    
    def task_b(ctx: DurableContext) -> str:
        return ctx.step(lambda _: "Result B")
    
    def task_c(ctx: DurableContext) -> str:
        return ctx.step(lambda _: "Result C")
    
    result: BatchResult[str] = context.parallel([task_a, task_b, task_c])
    
    results = result.get_results()
    
    # Access results by index
    first_result = results[0]   # "Result A"
    second_result = results[1]  # "Result B"
    third_result = results[2]   # "Result C"
    
    return {
        "first": first_result,
        "second": second_result,
        "third": third_result,
        "all": results,
    }
