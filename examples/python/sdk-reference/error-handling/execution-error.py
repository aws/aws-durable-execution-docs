from aws_durable_execution_sdk_python.context import DurableContext, StepContext, durable_step
from aws_durable_execution_sdk_python.exceptions import ExecutionError
from aws_durable_execution_sdk_python.execution import durable_execution


@durable_step
def process_data(step_context: StepContext, data: dict) -> dict:
    if not data.get("required_field"):
        raise ExecutionError("Required field missing")
    return {"processed": True}


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    result = context.step(process_data(event))
    return result
