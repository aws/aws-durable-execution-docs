from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
)

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Validate input and handle errors."""
    # Validate required fields
    if not event.get("user_id"):
        return {"error": "InvalidInput", "message": "user_id is required"}
    
    if not event.get("action"):
        return {"error": "InvalidInput", "message": "action is required"}
    
    # Process valid input
    user_id = event["user_id"]
    action = event["action"]
    
    result = context.step(
        lambda _: {"user_id": user_id, "action": action, "status": "completed"},
        name="process_action"
    )
    
    return result
