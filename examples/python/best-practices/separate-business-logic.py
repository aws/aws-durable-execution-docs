# business_logic.py
@durable_step
def validate_order(step_context: StepContext, order: dict) -> dict:
    if not order.get("items"):
        raise ValueError("Order must have items")
    return {**order, "validated": True}

# handler.py
@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    order = event["order"]
    validated_order = context.step(validate_order(order))
    return {"status": "completed", "order_id": validated_order["order_id"]}
