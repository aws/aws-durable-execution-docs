from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
)
from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.retries import (
    RetryStrategyConfig,
    create_retry_strategy,
)

@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    # Configure exponential backoff
    retry_config = RetryStrategyConfig(
        max_attempts=3,
        initial_delay_seconds=1,
        max_delay_seconds=10,
        backoff_rate=2.0,
    )
    
    step_config = StepConfig(
        retry_strategy=create_retry_strategy(retry_config)
    )
    
    result = context.step(
        lambda _: "Step with exponential backoff",
        name="retry_step",
        config=step_config,
    )
    return f"Result: {result}"
