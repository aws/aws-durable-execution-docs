import json

from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import CallbackConfig
from aws_durable_execution_sdk_python.serdes import SerDes, SerDesContext


class ApprovalSerDes(SerDes[dict]):
    def serialize(self, value: dict, ctx: SerDesContext) -> str:
        return json.dumps(value)

    def deserialize(self, data: str, ctx: SerDesContext) -> dict:
        return json.loads(data)


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    callback = context.create_callback(
        "await-approval",
        config=CallbackConfig(serdes=ApprovalSerDes()),
    )

    # Send callback.callback_id to the external system here.
    result = callback.result()
    return result
