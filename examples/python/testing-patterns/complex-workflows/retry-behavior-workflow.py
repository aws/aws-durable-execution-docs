from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.retries import (
    RetryStrategyConfig,
    create_retry_strategy,
)

attempt_count = 0

@durable_step
def unreliable_operation(step_context: StepContext) -> str:
    global attempt_count
    attempt_count += 1
    
    if attempt_count < 3:
        raise RuntimeError("Transient error")
    
    return "Operation succeeded"

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    retry_config = RetryStrategyConfig(
        max_attempts=5,
        retryable_error_types=[RuntimeError],
    )
    
    result = context.step(
        unreliable_operation(),
        config=StepConfig(create_retry_strategy(retry_config)),
        name="unreliable"
    )
    
    return result
