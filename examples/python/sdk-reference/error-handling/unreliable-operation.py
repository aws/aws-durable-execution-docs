from random import random

from aws_durable_execution_sdk_python import (
    DurableContext,
    StepContext,
    durable_execution,
    durable_step,
)
from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.retries import (
    RetryStrategyConfig,
    create_retry_strategy,
)


@durable_step
def unreliable_operation(step_context: StepContext) -> str:
    if random() > 0.5:
        raise RuntimeError("Random error occurred")
    return "Operation succeeded"


@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    retry_config = RetryStrategyConfig(
        max_attempts=3,
        retryable_error_types=[RuntimeError],
    )

    result = context.step(
        unreliable_operation(),
        config=StepConfig(retry_strategy=create_retry_strategy(retry_config)),
    )

    return result
