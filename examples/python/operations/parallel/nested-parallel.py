from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)


def group_a(ctx: DurableContext) -> list[str]:
    inner: BatchResult[str] = ctx.parallel(
        [
            lambda c: c.step(lambda _: "a1", name="a1"),
            lambda c: c.step(lambda _: "a2", name="a2"),
        ],
        name="inner-a",
    )
    return inner.get_results()


def group_b(ctx: DurableContext) -> list[str]:
    inner: BatchResult[str] = ctx.parallel(
        [
            lambda c: c.step(lambda _: "b1", name="b1"),
            lambda c: c.step(lambda _: "b2", name="b2"),
        ],
        name="inner-b",
    )
    return inner.get_results()


@durable_execution
def handler(event: dict, context: DurableContext) -> list[list[str]]:
    outer: BatchResult[list[str]] = context.parallel(
        [group_a, group_b],
        name="outer",
    )
    return outer.to_dict()
