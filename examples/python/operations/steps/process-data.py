from aws_durable_execution_sdk_python import (
    DurableContext,
    StepContext,
    durable_execution,
    durable_step,
)
from aws_durable_execution_sdk_python.config import StepConfig, StepSemantics
from aws_durable_execution_sdk_python.retries import (
    RetryStrategyConfig,
    create_retry_strategy,
)


@durable_step
def process_data(step_context: StepContext, data: str) -> dict:
    return {"processed": data, "status": "completed"}


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    retry_config = RetryStrategyConfig(
        max_attempts=3,
        retryable_error_types=[RuntimeError, ValueError],
    )

    step_config = StepConfig(
        retry_strategy=create_retry_strategy(retry_config),
        step_semantics=StepSemantics.AT_LEAST_ONCE_PER_RETRY,
    )

    result = context.step(process_data(event["data"]), config=step_config)
    return result
