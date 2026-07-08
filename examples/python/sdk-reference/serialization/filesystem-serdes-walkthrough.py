from aws_durable_execution_sdk_python import (
    DurableContext,
    StepContext,
    durable_execution,
    durable_step,
)
from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.filesystem_serdes import FileSystemSerDes


@durable_step
def fetch_order(ctx: StepContext) -> dict:
    return {"id": "order-123", "total": "99.99"}


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    fs_serdes = FileSystemSerDes("/mnt/s3")

    # Pass the FileSystem serdes to one step. Other operations in this handler
    # keep using the default serdes.
    order = context.step(
        fetch_order(),
        config=StepConfig(serdes=fs_serdes),
    )

    return order
