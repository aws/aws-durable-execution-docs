from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_step,
    ExecutionError,
    InvocationError,
    ValidationError,
    StepContext,
)

@durable_step
def complex_operation(step_context: StepContext, data: dict) -> dict:
    """Operation with multiple failure modes."""
    # Validate input
    if not data:
        raise ValueError("Data is required")
    
    # Check business rules
    if data.get("amount", 0) < 0:
        raise ExecutionError("Amount must be positive")
    
    # Call external service
    try:
        result = call_external_service(data)
        return result
    except ConnectionError:
        # Transient failure
        raise InvocationError("Service unavailable")

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Handle multiple error types."""
    try:
        result = context.step(complex_operation(event))
        return {"status": "success", "result": result}
    except ValueError as e:
        return {"status": "invalid", "error": str(e)}
    except ExecutionError as e:
        return {"status": "failed", "error": str(e)}
    except InvocationError as e:
        # Let Lambda retry
        raise
