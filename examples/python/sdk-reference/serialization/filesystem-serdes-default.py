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


@durable_step
def fetch_preferences(ctx: StepContext) -> dict:
    return {"theme": "dark", "language": "en"}


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Create once and pass to every operation in the handler.
    fs_serdes = FileSystemSerDes("/mnt/s3")
    config = StepConfig(serdes=fs_serdes)

    order = context.step(fetch_order(), config=config)
    preferences = context.step(fetch_preferences(), config=config)

    return {"order": order, "preferences": preferences}
