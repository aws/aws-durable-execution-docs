from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import Duration

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    """Durable function with a named wait."""
    # Wait with explicit name
    context.wait(duration=Duration.from_seconds(2), name="custom_wait")
    return "Wait with name completed"
