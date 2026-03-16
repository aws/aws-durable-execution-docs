from typing import Any
from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import CallbackConfig, Duration

@durable_execution
def handler(event: Any, context: DurableContext) -> dict:
    """Create a callback and wait for external system response."""
    # Step 1: Create the callback
    callback_config = CallbackConfig(
        timeout=Duration.from_minutes(2),
        heartbeat_timeout=Duration.from_seconds(60),
    )
    
    callback = context.create_callback(
        name="example_callback",
        config=callback_config,
    )
    
    # Step 2: Send callback ID to external system
    # In a real scenario, you'd send this to a third-party API,
    # message queue, or webhook endpoint
    send_to_external_system({
        "callback_id": callback.callback_id,
        "data": event.get("data"),
    })
    
    # Step 3: Wait for the result - execution suspends here
    result = callback.result()
    
    # Step 4: Execution resumes when result is received
    return {
        "status": "completed",
        "result": result,
    }
