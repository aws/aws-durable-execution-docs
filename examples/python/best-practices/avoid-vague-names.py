@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    # Generic names don't help with debugging
    user = context.step(fetch_user(event["user_id"]), name="step1")
    validated = context.step(validate_user(user), name="step2")
    notification = context.step(send_notification(user), name="step3")
    return {"status": "completed"}
