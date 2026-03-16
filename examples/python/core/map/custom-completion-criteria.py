from aws_durable_execution_sdk_python.config import CompletionConfig, MapConfig


def process_item(
    context: DurableContext, item: int, index: int, items: list[int]
) -> dict:
    """Process an item that might fail."""
    # Processing that might fail
    if item % 7 == 0:
        raise ValueError(f"Item {item} failed")
    return {"item": item, "processed": True}


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    items = list(range(20))

    # Succeed if at least 15 items succeed, fail after 5 failures
    config = MapConfig(
        completion_config=CompletionConfig(
            min_successful=15,
            tolerated_failure_count=5,
        )
    )

    result = context.map(items, process_item, config=config)
    return result.to_dict()
