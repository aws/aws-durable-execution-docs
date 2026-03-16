@durable_with_child_context
def inventory_check(ctx: DurableContext, items: list) -> dict:
    """Check inventory for all items."""
    results = []
    for item in items:
        available = ctx.step(
            lambda _: check_item_availability(item),
            name=f"check_{item['id']}"
        )
        results.append({"item_id": item["id"], "available": available})
    
    return {"all_available": all(r["available"] for r in results)}

@durable_with_child_context
def payment_processing(ctx: DurableContext, order_total: float) -> dict:
    """Process payment in isolated context."""
    auth = ctx.step(
        lambda _: authorize_payment(order_total),
        name="authorize"
    )
    
    if auth["approved"]:
        capture = ctx.step(
            lambda _: capture_payment(auth["transaction_id"]),
            name="capture"
        )
        return {"status": "completed", "transaction_id": capture["id"]}
    
    return {"status": "declined"}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Process order with organized child contexts."""
    # Check inventory
    inventory = context.run_in_child_context(
        inventory_check(event["items"]),
        name="inventory_check"
    )
    
    if not inventory["all_available"]:
        return {"status": "failed", "reason": "items_unavailable"}
    
    # Process payment
    payment = context.run_in_child_context(
        payment_processing(event["total"]),
        name="payment_processing"
    )
    
    if payment["status"] != "completed":
        return {"status": "failed", "reason": "payment_declined"}
    
    return {
        "status": "success",
        "transaction_id": payment["transaction_id"],
    }
