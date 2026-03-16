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
    # Your code here
    return "success"

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    # Configure retry strategy
    retry_config = RetryStrategyConfig(
        max_attempts=3,
        initial_delay_seconds=1,
        max_delay_seconds=10,
        backoff_rate=2.0,
        retryable_error_types=[RuntimeError, ConnectionError],
    )
    
    # Create step config with retry
    step_config = StepConfig(
        retry_strategy=create_retry_strategy(retry_config)
    )
    
    # Execute with retry
    result = context.step(unreliable_operation(), config=step_config)
    return result
