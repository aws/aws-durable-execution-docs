from aws_durable_execution_sdk_python.config import StepConfig, StepSemantics
from aws_durable_execution_sdk_python.retries import RetryPresets

PAYMENT_CONFIG = StepConfig(
    step_semantics=StepSemantics.AT_MOST_ONCE_PER_RETRY,
    retry_strategy=RetryPresets.none(),
)
IDEMPOTENT_CONFIG = StepConfig(retry_strategy=RetryPresets.default())

context.step(charge_step(order), config=PAYMENT_CONFIG)
context.step(refund_step(order), config=PAYMENT_CONFIG)
context.step(fetch_user(id), config=IDEMPOTENT_CONFIG)
