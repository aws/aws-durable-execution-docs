import json

from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import MapConfig
from aws_durable_execution_sdk_python.serdes import SerDes, SerDesContext


class ItemSerDes(SerDes[dict]):
    def serialize(self, value: dict, ctx: SerDesContext) -> str:
        return json.dumps(value)

    def deserialize(self, data: str, ctx: SerDesContext) -> dict:
        return json.loads(data)


@durable_execution
def handler(event: dict, context: DurableContext) -> list:
    items = ["a", "b", "c"]
    result = context.map(
        items,
        lambda ctx, item, idx, arr: {"id": item, "status": "done"},
        config=MapConfig(item_serdes=ItemSerDes()),
    )
    return result.get_results()
