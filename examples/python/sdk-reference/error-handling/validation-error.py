from aws_durable_execution_sdk_python.config import Duration
from aws_durable_execution_sdk_python.context import DurableContext
from aws_durable_execution_sdk_python.exceptions import ValidationError
from aws_durable_execution_sdk_python.execution import durable_execution


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    # The SDK raises ValidationError for invalid configuration values.
    # For example, passing a negative duration:
    try:
        duration = Duration(seconds=-1)  # invalid
    except ValidationError as e:
        context.logger.error("Invalid SDK configuration", extra={"message": str(e)})
        return {"error": "InvalidConfiguration", "message": str(e)}
    return {"status": "ok"}
