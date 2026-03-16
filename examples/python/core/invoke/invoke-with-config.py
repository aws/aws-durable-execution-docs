from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import Duration, InvokeConfig

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Configure invoke with timeout
    invoke_config = InvokeConfig(
        timeout=Duration.from_minutes(5),
    )
    
    result = context.invoke(
        function_name="long-running-function",
        payload=event,
        name="long_running",
        config=invoke_config,
    )
    
    return result
