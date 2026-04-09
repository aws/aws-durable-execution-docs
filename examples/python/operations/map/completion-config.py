from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import CompletionConfig, MapConfig


def process_item(
    ctx: DurableContext, item: str, index: int, items: list[str]
) -> str:
    return ctx.step(lambda _: item.upper(), name=f"process-{index}")


@durable_execution
def handler(event: dict, context: DurableContext) -> list[str]:
    config = MapConfig(
        completion_config=CompletionConfig(min_successful=3),
    )
    result: BatchResult[str] = context.map(
        event["items"],
        process_item,
        name="process-items",
        config=config,
    )
    return result.to_dict()
