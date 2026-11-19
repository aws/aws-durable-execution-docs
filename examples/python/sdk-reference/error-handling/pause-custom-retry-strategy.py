from aws_durable_execution_sdk_python.config import Duration, StepConfig
from aws_durable_execution_sdk_python.context import DurableContext, StepContext, durable_step
from aws_durable_execution_sdk_python.execution import durable_execution
from aws_durable_execution_sdk_python.retries import RetryDecision


class DownstreamUnavailableError(Exception):
    pass


def retry_strategy(error: Exception, attempt_count: int) -> RetryDecision:
    if attempt_count >= 10:
        return RetryDecision.no_retry()
    return RetryDecision.retry(
        Duration.from_seconds(2),
        pause=attempt_count == 5 and isinstance(error, DownstreamUnavailableError),
    )


step_config = StepConfig(retry_strategy=retry_strategy)


@durable_step
def call_downstream(step_context: StepContext) -> str:
    return "ok"


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> str:
    return context.step(call_downstream(), config=step_config)
