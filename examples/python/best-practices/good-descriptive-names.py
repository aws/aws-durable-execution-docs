@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    user = context.step(fetch_user(event["user_id"]), name="fetch_user")
    validated = context.step(validate_user(user), name="validate_user")
    notification = context.step(send_notification(user), name="send_notification")
    return {"status": "completed"}
