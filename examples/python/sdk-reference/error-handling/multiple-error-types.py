from aws_durable_execution_sdk_python.context import DurableContext, StepContext, durable_step
from aws_durable_execution_sdk_python.exceptions import ExecutionError, InvocationError
from aws_durable_execution_sdk_python.execution import durable_execution


@durable_step
def complex_operation(step_context: StepContext, data: dict) -> dict:
    if not data:
        raise ValueError("Data is required")
    if data.get("amount", 0) < 0:
        raise ExecutionError("Amount must be positive")
    try:
        result = call_external_service(data)
        return result
    except ConnectionError:
        raise InvocationError("Service unavailable")


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    try:
        result = context.step(complex_operation(event))
        return {"status": "success", "result": result}
    except ValueError as e:
        return {"status": "invalid", "error": str(e)}
    except ExecutionError as e:
        return {"status": "failed", "error": str(e)}
    except InvocationError:
        raise


def call_external_service(data: dict) -> dict:
    return {"processed": True}
