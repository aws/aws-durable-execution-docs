from aws_durable_execution_sdk_python import (
    DurableContext,
    StepContext,
    durable_execution,
    durable_step,
)
from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.serdes import PassThroughSerDes


@durable_step
def fetch_raw(ctx: StepContext) -> str:
    return '{"id":"order-123"}'


@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    raw = context.step(
        fetch_raw(),
        config=StepConfig(serdes=PassThroughSerDes()),
    )
    return raw
