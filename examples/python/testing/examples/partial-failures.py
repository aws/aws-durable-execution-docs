from aws_durable_execution_sdk_python import DurableContext, durable_execution, durable_step
from aws_durable_execution_sdk_python.types import StepContext
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python.lambda_service import OperationStatus
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner


@durable_step
def step_1(ctx: StepContext) -> str:
    return "ok"


@durable_step
def step_2(ctx: StepContext) -> str:
    return "ok"


@durable_step
def step_3(ctx: StepContext) -> str:
    raise RuntimeError("step-3 failed")


@durable_execution
def handler(event, context: DurableContext):
    context.step(step_1(), name="step-1")
    context.step(step_2(), name="step-2")
    context.step(step_3(), name="step-3")


def test_records_which_steps_succeeded_before_failure():
    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        result = runner.run(timeout=10)

    assert result.status is InvocationStatus.FAILED

    s1 = result.get_step("step-1")
    assert s1.status is OperationStatus.SUCCEEDED

    s2 = result.get_step("step-2")
    assert s2.status is OperationStatus.SUCCEEDED

    assert result.error is not None
