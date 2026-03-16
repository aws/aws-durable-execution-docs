from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    BatchResult,
)
from aws_durable_execution_sdk_python.config import (
    MapConfig,
    CompletionConfig,
)

def process_item(context: DurableContext, item: int, index: int, items: list[int]) -> dict:
    """Process a single item."""
    return {"item": item, "squared": item * item}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    items = list(range(100))

    # Configure map operation
    config = MapConfig(
        max_concurrency=10,  # Process 10 items at a time
        completion_config=CompletionConfig.all_successful(),  # Require all to succeed
    )

    result = context.map(items, process_item, name="process_numbers", config=config)
    return result.to_dict()
