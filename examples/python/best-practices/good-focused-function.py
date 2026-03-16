@durable_execution
def process_order(event: dict, context: DurableContext) -> dict:
    """Process a single order through validation, payment, and fulfillment."""
    order_id = event["order_id"]
    
    validation = context.step(validate_order(order_id))
    payment = context.step(process_payment(order_id, event["amount"]))
    fulfillment = context.step(fulfill_order(order_id))
    
    return {"order_id": order_id, "status": "completed"}
