from aws_durable_execution_sdk_python import DurableContext, StepContext, durable_execution, durable_step
from aws_durable_execution_sdk_python.config import Duration


@durable_step
def fetch_data(ctx: StepContext, id: str) -> str:
    return f"data-for-{id}"


@durable_step
def process_data(ctx: StepContext, data: str) -> str:
    return f"processed-{data}"


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Step 1: Fetch data — result is checkpointed
    data = context.step(fetch_data(event["id"]))

    # Step 2: Wait 30 seconds without consuming compute resources
    context.wait(Duration.from_seconds(30))

    # Step 3: Process the data — only runs after the wait completes
    result = context.step(process_data(data))

    return result
