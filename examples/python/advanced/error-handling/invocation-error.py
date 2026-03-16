from aws_durable_execution_sdk_python import InvocationError

@durable_step
def call_external_service(step_context: StepContext) -> dict:
    """Call external service with retry."""
    try:
        # Call external service
        response = make_api_call()
        return response
    except ConnectionError:
        # Trigger Lambda retry
        raise InvocationError("Service unavailable")
