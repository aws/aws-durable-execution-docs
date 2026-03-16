from aws_durable_execution_sdk_python import durable_step, StepContext

@durable_step
def fetch_user_data(step_context: StepContext, user_id: str) -> dict:
    """Fetch user data from external service."""
    return {"user_id": user_id, "name": "Jane Doe", "email": "jane_doe@example.com"}

@durable_step
def send_notification(step_context: StepContext, user: dict) -> dict:
    """Send notification to user."""
    return {"sent": True, "email": user["email"]}

def process_user(
    context: DurableContext,
    user_id: str,
    index: int,
    user_ids: list[str]
) -> dict:
    """Process a user by fetching data and sending notification."""
    # Use steps within the map function
    user = context.step(fetch_user_data(user_id))
    notification = context.step(send_notification(user))
    return {"user_id": user_id, "notification_sent": notification["sent"]}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Process multiple users using context operations within map functions."""
    user_ids = ["user_1", "user_2", "user_3"]

    result = context.map(user_ids, process_user)
    # Convert to dict for JSON serialization (BatchResult is not JSON serializable)
    return result.to_dict()
