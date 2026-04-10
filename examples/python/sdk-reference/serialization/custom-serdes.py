import json

from aws_durable_execution_sdk_python.serdes import SerDes, SerDesContext


class OrderSerDes(SerDes[dict]):
    def serialize(self, value: dict, ctx: SerDesContext) -> str:
        return json.dumps(value)

    def deserialize(self, data: str, ctx: SerDesContext) -> dict:
        return json.loads(data)
