from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)


def task_a(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "a done", name="run-a")


def task_b(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "b done", name="run-b")


@durable_execution
def handler(event: dict, context: DurableContext) -> list[str]:
    result: BatchResult[str] = context.parallel(
        [task_a, task_b],
        name="process",
    )
    return result.to_dict()
