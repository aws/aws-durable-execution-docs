from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)


def process_item(
    ctx: DurableContext, item: str, index: int, items: list[str]
) -> str:
    return ctx.step(lambda _: item.upper(), name=f"item-{index}")


def process_region(
    ctx: DurableContext, region: dict, index: int, regions: list[dict]
) -> list[str]:
    inner: BatchResult[str] = ctx.map(
        region["items"],
        process_item,
        name=f"process-{region['name']}",
    )
    return inner.get_results()


@durable_execution
def handler(event: dict, context: DurableContext) -> list[list[str]]:
    result: BatchResult[list[str]] = context.map(
        event["regions"],
        process_region,
        name="process-regions",
    )
    return result.to_dict()
