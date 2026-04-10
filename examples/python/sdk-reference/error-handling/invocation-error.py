from aws_durable_execution_sdk_python.context import DurableContext, StepContext, durable_step
from aws_durable_execution_sdk_python.exceptions import InvocationError
from aws_durable_execution_sdk_python.execution import durable_execution


@durable_step
def call_external_service(step_context: StepContext) -> dict:
    try:
        response = make_api_call()
        return response
    except ConnectionError:
        raise InvocationError("Service unavailable")


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    result = context.step(call_external_service())
    return result


def make_api_call() -> dict:
    return {"status": "ok"}
