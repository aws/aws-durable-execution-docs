from aws_durable_execution_sdk_python import durable_step
from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.retries import (
    RetryStrategyConfig,
    create_retry_strategy,
)
from aws_durable_execution_sdk_python.types import StepContext


class TransientApiError(Exception):
    pass


class RateLimitError(Exception):
    pass


@durable_step
def call_api(ctx: StepContext, record_id: str) -> dict:
    return external_api.get(record_id)


# Retry only transient errors. Anything else (bad input, not-found) fails immediately.
retry_strategy = create_retry_strategy(
    RetryStrategyConfig(
        max_attempts=5,
        retryable_error_types=[TransientApiError, RateLimitError],
    )
)

context.step(
    call_api(event["id"]),
    config=StepConfig(retry_strategy=retry_strategy),
)
