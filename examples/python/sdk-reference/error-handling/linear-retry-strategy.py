from aws_durable_execution_sdk_python.config import Duration, StepConfig
from aws_durable_execution_sdk_python.context import DurableContext, StepContext, durable_step
from aws_durable_execution_sdk_python.execution import durable_execution
from aws_durable_execution_sdk_python.retries import (
    LinearRetryStrategyConfig,
    create_linear_retry_strategy,
)

retry_strategy = create_linear_retry_strategy(
    LinearRetryStrategyConfig(
        max_attempts=5,
        initial_delay=Duration.from_seconds(2),
        increment=Duration.from_seconds(3),
        max_delay=Duration.from_seconds(30),
    )
)

step_config = StepConfig(retry_strategy=retry_strategy)


@durable_step
def call_external_api(step_context: StepContext) -> str:
    return "ok"


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> str:
    return context.step(call_external_api(), config=step_config)
