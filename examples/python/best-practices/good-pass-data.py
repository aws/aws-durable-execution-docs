@durable_step
def fetch_user(step_context: StepContext, user_id: str) -> dict:
    return {"user_id": user_id, "name": "Jane Doe"}

@durable_step
def send_email(step_context: StepContext, user: dict) -> bool:
    send_to_address(user["name"], user.get("email"))
    return True

@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    user = context.step(fetch_user(event["user_id"]))
    sent = context.step(send_email(user))
    return {"sent": sent}
