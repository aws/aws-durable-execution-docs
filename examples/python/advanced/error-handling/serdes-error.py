from aws_durable_execution_sdk_python import SerDesError

@durable_step
def process_complex_data(step_context: StepContext, data: object) -> dict:
    """Process data that might not be serializable."""
    try:
        # Process data
        return {"result": data}
    except SerDesError as e:
        # Handle serialization failure
        return {"error": "Cannot serialize result"}
