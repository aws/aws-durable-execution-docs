from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import CompletionConfig, ParallelConfig


def source_a(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "result from a", name="source-a")


def source_b(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "result from b", name="source-b")


def source_c(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "result from c", name="source-c")


@durable_execution
def handler(event: dict, context: DurableContext) -> str | None:
    result: BatchResult[str] = context.parallel(
        [source_a, source_b, source_c],
        name="race",
        config=ParallelConfig(completion_config=CompletionConfig.first_successful()),
    )
    results = result.get_results()
    return results[0] if results else None
