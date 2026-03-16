from aws_durable_execution_sdk_python import DurableContext, durable_execution

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    """Simple hello world durable function."""
    return "Hello World!"
