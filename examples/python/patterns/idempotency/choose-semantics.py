from aws_durable_execution_sdk_python.config import StepConfig, StepSemantics
from aws_durable_execution_sdk_python.retries import RetryPresets

# At-least-once (default) for a retryable idempotent write.
context.step(upsert_user(event["user"]), name="upsert-user")

# At-most-once for a side-effecting call, with retries disabled.
context.step(
    charge_payment(event["amount"], event["card_token"]),
    name="charge-payment",
    config=StepConfig(
        step_semantics=StepSemantics.AT_MOST_ONCE_PER_RETRY,
        retry_strategy=RetryPresets.none(),
    ),
)
