@durable_step
def fetch_user(step_context: StepContext, user_id: str) -> dict:
    """Fetch user data."""
    return {"user_id": user_id, "name": "Jane Doe", "email": "jane_doe@example.com"}

@durable_step
def validate_user(step_context: StepContext, user: dict) -> bool:
    """Validate user data."""
    return user.get("email") is not None

@durable_step
def send_notification(step_context: StepContext, user: dict) -> dict:
    """Send notification to user."""
    return {"sent": True, "email": user["email"]}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    user_id = event["user_id"]
    
    # Step 1: Fetch user
    user = context.step(fetch_user(user_id))
    
    # Step 2: Validate user
    is_valid = context.step(validate_user(user))
    
    if not is_valid:
        return {"status": "failed", "reason": "invalid_user"}
    
    # Step 3: Send notification
    notification = context.step(send_notification(user))
    
    return {
        "status": "completed",
        "user_id": user_id,
        "notification_sent": notification["sent"],
    }
