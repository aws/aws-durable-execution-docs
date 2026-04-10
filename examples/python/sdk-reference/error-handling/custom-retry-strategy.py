from aws_durable_execution_sdk_python.config import Duration, StepConfig
from aws_durable_execution_sdk_python.context import DurableContext, StepContext, durable_step
from aws_durable_execution_sdk_python.execution import durable_execution
from aws_durable_execution_sdk_python.retries import RetryDecision


# A retry strategy is a plain callable: (Exception, int) -> RetryDecision
# attempt_count is 1-based: 1 on the first retry, 2 on the second, etc.
def custom_retry_strategy(error: Exception, attempt_count: int) -> RetryDecision:
    if attempt_count >= 4:
        return RetryDecision.no_retry()
    # Fixed 2-second delay regardless of attempt number
    return RetryDecision.retry(Duration.from_seconds(2))


step_config = StepConfig(retry_strategy=custom_retry_strategy)


@durable_step
def call_api(step_context: StepContext) -> str:
    return "ok"


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> str:
    return context.step(call_api(), config=step_config)
