@durable_step
def process_payment(step_context: StepContext, amount: float) -> dict:
    return {"status": "completed", "amount": amount}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Step is automatically named "process_payment"
    result = context.step(process_payment(100.0))
    return result
