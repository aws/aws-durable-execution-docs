from aws_durable_execution_sdk_python import durable_step, StepContext

@durable_step
def validate_order(step_context: StepContext, order_id: str) -> dict:
    """Validate an order."""
    # Your validation logic here
    return {"order_id": order_id, "valid": True}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    order_id = event["order_id"]
    validation = context.step(validate_order(order_id))
    return validation
