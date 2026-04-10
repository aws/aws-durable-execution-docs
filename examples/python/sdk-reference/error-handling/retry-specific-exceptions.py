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
def call_api(step_context: StepContext) -> dict:
    """Call external API that might fail."""
    if random() > 0.5:
        raise ConnectionError("Network timeout")
    return {"status": "success"}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Only retry ConnectionError, not other exceptions
    retry_config = RetryStrategyConfig(
        max_attempts=3,
        retryable_error_types=[ConnectionError],
    )
    
    result = context.step(
        call_api(),
        config=StepConfig(create_retry_strategy(retry_config)),
    )
    
    return result
