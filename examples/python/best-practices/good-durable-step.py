@durable_step
def validate_input(step_context: StepContext, data: dict) -> bool:
    return all(key in data for key in ["name", "email"])

@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    is_valid = context.step(validate_input(event))
    return {"valid": is_valid}
