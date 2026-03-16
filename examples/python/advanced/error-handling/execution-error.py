from aws_durable_execution_sdk_python import ExecutionError

@durable_step
def process_data(step_context: StepContext, data: dict) -> dict:
    """Process data with business logic validation."""
    if not data.get("required_field"):
        raise ExecutionError("Required field missing")
    return {"processed": True}
