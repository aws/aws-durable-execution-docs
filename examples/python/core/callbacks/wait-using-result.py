from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import CallbackConfig, Duration

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Create callback
    callback = context.create_callback(
        name="approval_callback",
        config=CallbackConfig(timeout=Duration.from_hours(24)),
    )
    
    # Send callback ID to approval system
    send_approval_request(callback.callback_id, event["request_details"])
    
    # Wait for approval response
    approval_result = callback.result()
    
    if approval_result and approval_result.get("approved"):
        return {"status": "approved", "details": approval_result}
    else:
        return {"status": "rejected"}
