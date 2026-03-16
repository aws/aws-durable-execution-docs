@durable_step
def validate_input(step_context: StepContext, value: int) -> int:
    if value < 0:
        raise ValueError("Value must be non-negative")
    return value

@durable_execution
def handler(event: dict, context: DurableContext) -> int:
    value = event.get("value", 0)
    validated = context.step(validate_input(value), name="validate")
    return validated
