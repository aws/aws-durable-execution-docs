from random import random
from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_step,
    StepContext,
)
from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.retries import (
    RetryStrategyConfig,
    create_retry_strategy,
)

@durable_step
def unreliable_operation(step_context: StepContext) -> str:
    """Operation that might fail."""
    if random() > 0.5:
        raise RuntimeError("Random error occurred")
    return "Operation succeeded"

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    # Only retry RuntimeError, not other exceptions
    retry_config = RetryStrategyConfig(
        max_attempts=3,
        retryable_error_types=[RuntimeError],
    )
    
    result = context.step(
        unreliable_operation(),
        config=StepConfig(create_retry_strategy(retry_config)),
    )
    
    return result
