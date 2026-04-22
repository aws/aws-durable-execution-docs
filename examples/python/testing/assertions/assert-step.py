from aws_durable_execution_sdk_python import DurableContext, durable_execution, durable_step
from aws_durable_execution_sdk_python.types import StepContext
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python.lambda_service import OperationStatus
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner


@durable_step
def compute(ctx: StepContext) -> int:
    return 42


@durable_execution
def handler(event, context: DurableContext) -> int:
    return context.step(compute())


def test_asserts_on_step_operation():
    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        result = runner.run(timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED

    step = result.get_step("compute")
    assert step.status is OperationStatus.SUCCEEDED
    assert step.result is not None
