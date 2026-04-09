from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)


def make_branch(item: str):
    def branch(ctx: DurableContext) -> str:
        return ctx.step(lambda _: f"processed {item}", name=f"process-{item}")

    return branch


@durable_execution
def handler(event: dict, context: DurableContext) -> list[str]:
    items = ["a", "b", "c"]
    result: BatchResult[str] = context.parallel(
        [make_branch(item) for item in items],
        name="process-items",
    )
    return result.to_dict()
