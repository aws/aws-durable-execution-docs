from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)


def square(ctx: DurableContext, item: int, index: int, items: list[int]) -> int:
    return ctx.step(lambda _: item * item, name=f"square-{index}")


@durable_execution
def handler(event: dict, context: DurableContext) -> list[int]:
    result: BatchResult[int] = context.map(
        [1, 2, 3, 4, 5],
        square,
        name="square-numbers",
    )
    return result.to_dict()
