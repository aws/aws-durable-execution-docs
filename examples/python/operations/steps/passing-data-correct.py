from aws_durable_execution_sdk_python import (
    DurableContext,
    StepContext,
    durable_execution,
    durable_step,
)
from aws_durable_execution_sdk_python.config import Duration


def register_user(email: str) -> str:
    return f"user-{email}"


def send_follow_up_email(user_id: str) -> None:
    # send email to user
    pass


# ✅ CORRECT: user_id is returned from the step and restored from checkpoint on replay
@durable_execution
def handler(event: dict, context: DurableContext) -> None:
    @durable_step
    def do_register(step_context: StepContext) -> str:
        return register_user(event["email"])

    user_id = context.step(do_register())

    context.wait(Duration.from_minutes(10), name="follow-up-delay")

    @durable_step
    def do_send(step_context: StepContext) -> None:
        send_follow_up_email(user_id)  # user_id restored from checkpoint

    context.step(do_send())
