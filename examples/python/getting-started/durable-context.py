from aws_durable_execution_sdk_python import DurableContext, durable_execution, durable_step, StepContext


@durable_step
def my_step(ctx: StepContext, data: dict) -> str:
    # Your business logic
    return f"step completed with {data}"


@durable_execution
def handler(event: dict, context: DurableContext):
    # Your function receives DurableContext instead of Lambda context
    # Use context.step(), context.wait(), etc.
    result = context.step(my_step(event["data"]))
    return result
