from aws_durable_execution_sdk_python.config import Duration, StepConfig
from aws_durable_execution_sdk_python.context import DurableContext, StepContext, durable_step
from aws_durable_execution_sdk_python.execution import durable_execution
from aws_durable_execution_sdk_python.retries import RetryDecision

PAUSE_FROM_ATTEMPT = 3


def retry_strategy(error: Exception, attempt_count: int) -> RetryDecision:
    return RetryDecision.retry(
        Duration.from_seconds(5),
        pause=attempt_count >= PAUSE_FROM_ATTEMPT,
    )


step_config = StepConfig(retry_strategy=retry_strategy)


@durable_step
def charge_payment(step_context: StepContext) -> str:
    return "ok"


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> str:
    return context.step(charge_payment(), config=step_config)
