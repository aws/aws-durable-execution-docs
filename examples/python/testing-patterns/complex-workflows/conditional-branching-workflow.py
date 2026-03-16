@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    amount = event.get("amount", 0)
    
    context.step(lambda _: amount, name="validate_amount")
    
    if amount > 1000:
        context.step(lambda _: "Manager approval required", name="approval")
        context.wait(Duration.from_seconds(10), name="approval_wait")
        result = context.step(lambda _: "High-value order processed", name="process_high")
    else:
        result = context.step(lambda _: "Standard order processed", name="process_standard")
    
    return result
