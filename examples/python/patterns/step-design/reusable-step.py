@durable_step
def validate_order(ctx: StepContext, order: dict) -> dict:
    return run_validation(order)


context.step(validate_order(order))
