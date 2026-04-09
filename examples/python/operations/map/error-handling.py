from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)


def process_item(
    ctx: DurableContext, item: str, index: int, items: list[str]
) -> str:
    def do_process(_):
        if item == "bad":
            raise ValueError("bad item")
        return item.upper()

    return ctx.step(do_process, name=f"process-{index}")


@durable_execution
def handler(event: dict, context: DurableContext) -> None:
    result: BatchResult[str] = context.map(
        event["items"],
        process_item,
        name="process-items",
    )

    if result.has_failure:
        errors = result.get_errors()
        print(f"{result.failure_count} items failed:", errors)

    successes = result.get_results()
    print(f"{result.success_count} items succeeded:", successes)
