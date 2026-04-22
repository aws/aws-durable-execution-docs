from aws_durable_execution_sdk_python import DurableContext, durable_execution, durable_step
from aws_durable_execution_sdk_python.types import StepContext
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner


@durable_step
def greet(ctx: StepContext) -> str:
    return "hello"


@durable_execution
def handler(event, context: DurableContext) -> str:
    return context.step(greet())


def test_returns_expected_result():
    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        result = runner.run(timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED
    assert result.result == '"hello"'
