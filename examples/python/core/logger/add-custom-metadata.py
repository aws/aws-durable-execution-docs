@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    order_id = event.get("order_id")
    
    context.logger.info(
        "Processing order",
        extra={
            "order_id": order_id,
            "customer_id": event.get("customer_id"),
            "priority": "high"
        }
    )
    
    result: str = context.step(
        lambda _: f"order-{order_id}-processed",
        name="process_order",
    )
    
    context.logger.info(
        "Order completed",
        extra={"order_id": order_id, "result": result}
    )
    
    return result
