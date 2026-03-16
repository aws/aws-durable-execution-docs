@durable_step
def process_order(step_context: StepContext, order: dict) -> dict:
    return {
        "order_id": order["id"],
        "total": 99.99,
        "items": ["item1", "item2"],
        "processed": True,
    }
