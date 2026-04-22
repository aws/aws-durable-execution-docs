from aws_durable_execution_sdk_python import DurableContext, durable_execution, durable_step
from aws_durable_execution_sdk_python.types import StepContext
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner


@durable_step
def premium_path(ctx: StepContext) -> str:
    return "premium"


@durable_step
def standard_path(ctx: StepContext) -> str:
    return "standard"


@durable_execution
def handler(event: dict, context: DurableContext) -> str:
    if event.get("premium"):
        return context.step(premium_path())
    return context.step(standard_path())


def test_takes_premium_path():
    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        result = runner.run(input='{"premium": true}', timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED
    assert result.result == '"premium"'


def test_takes_standard_path():
    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        result = runner.run(input='{"premium": false}', timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED
    assert result.result == '"standard"'
