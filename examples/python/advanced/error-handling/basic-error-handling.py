from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_step,
    StepContext,
)

@durable_step
def process_order(step_context: StepContext, order_id: str) -> dict:
    """Process an order with validation."""
    if not order_id:
        raise ValueError("Order ID is required")
    
    # Process the order
    return {"order_id": order_id, "status": "processed"}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Handle order processing with error handling."""
    try:
        order_id = event.get("order_id")
        result = context.step(process_order(order_id))
        return result
    except ValueError as e:
        # Handle validation errors from your code
        return {"error": "InvalidInput", "message": str(e)}
