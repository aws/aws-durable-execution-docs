from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)

@durable_execution
def handler(event: dict, context: DurableContext) -> list[str]:
    """Demonstrate result ordering."""
    
    functions = [
        lambda ctx: ctx.step(lambda _: "First"),
        lambda ctx: ctx.step(lambda _: "Second"),
        lambda ctx: ctx.step(lambda _: "Third"),
    ]
    
    result = context.parallel(functions)
    
    # Results are in the same order as functions
    results = result.get_results()
    assert results[0] == "First"
    assert results[1] == "Second"
    assert results[2] == "Third"
    
    return results
