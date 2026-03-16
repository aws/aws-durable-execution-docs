@durable_step
def calculate_total(step_context: StepContext, items: list) -> float:
    return sum(item["price"] for item in items)

@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> float:
    # Safe to run multiple times - same input produces same output
    total = context.step(calculate_total(event["items"]))
    return total
