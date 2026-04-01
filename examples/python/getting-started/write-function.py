from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_step,
    StepContext,
)


@durable_step
def my_step(step_context: StepContext, data: str) -> str:
    # Your business logic here
    return f"processed-{data}"


@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    result = context.step(my_step(event["data"]))
    return result
