from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    BatchResult,
)

def square(context: DurableContext, item: int, index: int, items: list[int]) -> int:
    """Square a number."""
    return item * item

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Process a list of items using map operations."""
    items = [1, 2, 3, 4, 5]

    result = context.map(items, square)
    # Convert to dict for JSON serialization (BatchResult is not JSON serializable)
    return result.to_dict()
