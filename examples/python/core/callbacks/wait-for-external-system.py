from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_step,
    StepContext,
)
from aws_durable_execution_sdk_python.config import (
    CallbackConfig,
    Duration,
    StepConfig,
)
from aws_durable_execution_sdk_python.retries import (
    RetryStrategyConfig,
    create_retry_strategy,
)

@durable_step
def wait_for_external_system(
    step_context: StepContext,
    callback_id: str,
) -> dict:
    """Wait for external system with retry on timeout."""
    # This will retry if the callback times out
    result = context.wait_for_callback(
        callback_id,
        config=CallbackConfig(timeout=Duration.from_minutes(2)),
    )
    return result

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Create callback
    callback = context.create_callback(name="external_api")
    
    # Send request
    send_external_request(callback.callback_id)
    
    # Wait with retry
    retry_config = RetryStrategyConfig(
        max_attempts=3,
        initial_delay_seconds=5,
    )
    
    result = context.step(
        wait_for_external_system(callback.callback_id),
        config=StepConfig(retry_strategy=create_retry_strategy(retry_config)),
    )
    
    return result
