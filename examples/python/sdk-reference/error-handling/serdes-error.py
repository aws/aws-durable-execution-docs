from aws_durable_execution_sdk_python.context import DurableContext, StepContext, durable_step
from aws_durable_execution_sdk_python.exceptions import SerDesError
from aws_durable_execution_sdk_python.execution import durable_execution


@durable_step
def build_result(step_context: StepContext) -> dict:
    # Return a value that can be serialized by the default serdes
    return {"message": "hello"}


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    # SerDesError is raised when the SDK cannot serialize or deserialize a value.
    # The default serdes handles standard Python types. Custom serdes implementations
    # should raise SerDesError when they encounter a value they cannot handle.
    try:
        result = context.step(build_result())
        return result
    except SerDesError as e:
        context.logger.error("Serialization failed", extra={"message": str(e)})
        raise
