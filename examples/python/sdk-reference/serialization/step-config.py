import json

from aws_durable_execution_sdk_python import (
    DurableContext,
    StepContext,
    durable_execution,
    durable_step,
)
from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.serdes import SerDes, SerDesContext


class OrderSerDes(SerDes[dict]):
    def serialize(self, value: dict, ctx: SerDesContext) -> str:
        return json.dumps(value)

    def deserialize(self, data: str, ctx: SerDesContext) -> dict:
        return json.loads(data)


@durable_step
def fetch_order(ctx: StepContext) -> dict:
    return {"id": "order-123", "total": "99.99"}


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    order = context.step(
        fetch_order(),
        config=StepConfig(serdes=OrderSerDes()),
    )
    return order
