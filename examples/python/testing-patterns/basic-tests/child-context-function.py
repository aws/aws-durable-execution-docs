from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_with_child_context,
)

@durable_with_child_context
def child_operation(ctx: DurableContext, value: int) -> int:
    return ctx.step(lambda _: value * 2, name="multiply")

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    result = context.run_in_child_context(child_operation(5))
    return f"Child context result: {result}"
