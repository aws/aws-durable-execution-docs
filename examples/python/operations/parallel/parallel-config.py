from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import CompletionConfig, ParallelConfig


def try_primary(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "primary result", name="primary")


def try_secondary(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "secondary result", name="secondary")


def try_cache(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "cache result", name="cache")


@durable_execution
def handler(event: dict, context: DurableContext) -> str | None:
    config = ParallelConfig(
        max_concurrency=2,
        completion_config=CompletionConfig.first_successful(),
    )
    result: BatchResult[str] = context.parallel(
        [try_primary, try_secondary, try_cache],
        name="fetch-data",
        config=config,
    )
    results = result.get_results()
    return results[0] if results else None
