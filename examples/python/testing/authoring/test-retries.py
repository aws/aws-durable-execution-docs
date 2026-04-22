from aws_durable_execution_sdk_python import DurableContext, durable_execution, durable_step
from aws_durable_execution_sdk_python.types import StepContext
from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.retries import create_retry_strategy, RetryStrategyConfig
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner

attempts = 0


@durable_step
def flaky(ctx: StepContext) -> str:
    global attempts
    attempts += 1
    if attempts < 3:
        raise RuntimeError("transient error")
    return "done"


@durable_execution
def handler(event, context: DurableContext) -> str:
    config = StepConfig(
        retry_strategy=create_retry_strategy(RetryStrategyConfig(max_attempts=3))
    )
    return context.step(flaky(), config=config)


def test_retries_and_eventually_succeeds():
    global attempts
    attempts = 0

    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        result = runner.run(timeout=30)

    assert result.status is InvocationStatus.SUCCEEDED
    assert result.result == '"done"'
