from aws_durable_execution_sdk_python import (
    DurableContext,
    StepContext,
    durable_execution,
    durable_step,
)


@durable_step
def fetch_order(ctx: StepContext) -> dict:
    return {"id": "order-123", "total": "99.99"}


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # No SerDes config — the SDK serializes and deserializes the result automatically.
    order = context.step(fetch_order())
    return order
