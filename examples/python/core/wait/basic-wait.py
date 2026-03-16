from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import Duration

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    """Simple durable function with a wait."""
    # Wait for 5 seconds
    context.wait(duration=Duration.from_seconds(5))
    return "Wait completed"
