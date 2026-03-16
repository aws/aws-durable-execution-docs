from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_with_child_context,
)

@durable_with_child_context
def process_order(ctx: DurableContext, order_id: str, items: list) -> dict:
    """Process an order in a child context."""
    # Validate items
    validation = ctx.step(
        lambda _: validate_items(items),
        name="validate_items"
    )
    
    if not validation["valid"]:
        return {"status": "invalid", "errors": validation["errors"]}
    
    # Calculate total
    total = ctx.step(
        lambda _: calculate_total(items),
        name="calculate_total"
    )
    
    # Process payment
    payment = ctx.step(
        lambda _: process_payment(order_id, total),
        name="process_payment"
    )
    
    return {
        "order_id": order_id,
        "total": total,
        "payment_status": payment["status"],
    }

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Process an order using a child context."""
    order_id = event["order_id"]
    items = event["items"]
    
    # Execute order processing in child context
    result = context.run_in_child_context(
        process_order(order_id, items)
    )
    
    return result
