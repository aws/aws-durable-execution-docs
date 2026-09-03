from aws_durable_execution_sdk_python import (
    BatchResult,
    CompletionStatus,
    DurableContext,
    complete_batch,
    continue_batch,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import CompletionConfig, ParallelConfig


def try_primary(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "primary result", name="primary")


def try_secondary(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "secondary result", name="secondary")


def stop_after_first_success(status: CompletionStatus):
    if status.success_count >= 1:
        return complete_batch()
    return continue_batch()


@durable_execution
def handler(event: dict, context: DurableContext) -> str | None:
    config = ParallelConfig(
        completion_config=CompletionConfig(should_complete=stop_after_first_success),
    )
    result: BatchResult[str] = context.parallel(
        [try_primary, try_secondary],
        name="fetch-data",
        config=config,
    )
    results = result.get_results()
    return results[0] if results else None
