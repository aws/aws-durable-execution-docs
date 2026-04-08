from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_step,
    durable_with_child_context,
    StepContext,
)

@durable_step
def validate_order(step_context: StepContext, order_id: str) -> dict:
    """Validate order details."""
    # Validation logic here
    return {"valid": True, "order_id": order_id}

@durable_step
def reserve_inventory(step_context: StepContext, order_id: str) -> dict:
    """Reserve inventory for order."""
    # Inventory logic here
    return {"reserved": True, "order_id": order_id}

@durable_step
def charge_payment(step_context: StepContext, order_id: str) -> dict:
    """Charge payment for order."""
    # Payment logic here
    return {"charged": True, "order_id": order_id}

@durable_step
def send_confirmation(step_context: StepContext, result: dict) -> dict:
    """Send order confirmation."""
    # Notification logic here
    return {"sent": True, "order_id": result["order_id"]}

@durable_with_child_context
def process_order(ctx: DurableContext, order_id: str) -> dict:
    """Process an order with multiple steps."""
    # These three steps execute as a single unit
    validation = ctx.step(validate_order(order_id))
    inventory = ctx.step(reserve_inventory(order_id))
    payment = ctx.step(charge_payment(order_id))
    
    return {"order_id": order_id, "status": "completed"}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Process order using a child context."""
    # Once this completes, it never replays - even if the function continues
    result = context.run_in_child_context(
        process_order(event["order_id"]),
        name="order_processing"
    )
    
    # Additional operations here won't cause process_order to replay
    context.step(send_confirmation(result))
    
    return result
