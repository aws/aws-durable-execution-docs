# config.py
from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.retries import (
    RetryStrategyConfig,
    create_retry_strategy,
)

FAST_RETRY = StepConfig(
    retry_strategy=create_retry_strategy(
        RetryStrategyConfig(
            max_attempts=3,
            initial_delay_seconds=1,
            max_delay_seconds=5,
            backoff_rate=2.0,
        )
    )
)

# handler.py
from config import FAST_RETRY

@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    data = context.step(fetch_data(event["id"]), config=FAST_RETRY)
    return data
