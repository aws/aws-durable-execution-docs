@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    # Lambda functions require explicit names and are harder to test
    is_valid = context.step(
        lambda _: all(key in event for key in ["name", "email"]),
        name="validate_input"
    )
    return {"valid": is_valid}
