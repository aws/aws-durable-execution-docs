from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.retries import (
    RetryStrategyConfig,
    create_retry_strategy,
)

@durable_step
def call_api(step_context: StepContext, url: str) -> dict:
    response = requests.get(url, timeout=10)
    response.raise_for_status()
    return response.json()

@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    retry_config = RetryStrategyConfig(
        max_attempts=3,
        retryable_error_types=[requests.Timeout, requests.ConnectionError],
    )
    
    result = context.step(
        call_api(event["url"]),
        config=StepConfig(retry_strategy=create_retry_strategy(retry_config)),
    )
    return result
