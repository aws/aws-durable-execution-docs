from aws_durable_execution_sdk_python import (
    DurableContext,
    StepContext,
    durable_execution,
    durable_step,
)


@durable_step
def add_numbers(step_context: StepContext, a: int, b: int) -> int:
    return a + b


@durable_execution
def handler(event: dict, context: DurableContext) -> int:
    result = context.step(add_numbers(5, 3))
    return result
