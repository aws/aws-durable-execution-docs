from aws_durable_execution_sdk_python import (
    BatchResult,
    CompletionStatus,
    DurableContext,
    complete_batch,
    continue_batch,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import CompletionConfig, MapConfig


def process_item(ctx: DurableContext, item: str, index: int, items: list[str]) -> str:
    return ctx.step(lambda _: item.upper(), name=f"process-{index}")


def stop_after_two_successes(status: CompletionStatus):
    if status.success_count >= 2:
        return complete_batch()
    return continue_batch()


@durable_execution
def handler(event: dict, context: DurableContext) -> list[str]:
    config = MapConfig(
        completion_config=CompletionConfig(should_complete=stop_after_two_successes),
    )
    result: BatchResult[str] = context.map(
        event["items"],
        process_item,
        name="process-items",
        config=config,
    )
    return result.get_results()
