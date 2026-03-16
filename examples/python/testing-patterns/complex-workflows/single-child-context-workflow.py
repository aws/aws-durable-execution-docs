from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_with_child_context,
)

@durable_with_child_context
def process_item(ctx: DurableContext, item_id: str) -> dict:
    ctx.step(lambda _: f"Validating {item_id}", name="validate")
    result = ctx.step(
        lambda _: {"item_id": item_id, "status": "processed"},
        name="process"
    )
    return result

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    item_id = event["item_id"]
    result = context.run_in_child_context(
        process_item(item_id),
        name="item_processing"
    )
    return result
