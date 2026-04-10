from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.context import DurableContext, StepContext, durable_step
from aws_durable_execution_sdk_python.execution import durable_execution
from aws_durable_execution_sdk_python.retries import RetryStrategyConfig, create_retry_strategy


class RateLimitError(Exception):
    pass


class ServiceUnavailableError(Exception):
    pass


retry_strategy = create_retry_strategy(
    RetryStrategyConfig(
        max_attempts=5,
        # Only retry these specific error types; all other errors fail immediately
        retryable_error_types=[RateLimitError, ServiceUnavailableError],
    )
)

step_config = StepConfig(retry_strategy=retry_strategy)


@durable_step
def call_api(step_context: StepContext) -> str:
    # Raises RateLimitError or ServiceUnavailableError on transient failures
    return "ok"


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> str:
    return context.step(call_api(), config=step_config)
