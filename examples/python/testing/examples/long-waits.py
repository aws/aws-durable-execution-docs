import os

os.environ["DURABLE_EXECUTION_TIME_SCALE"] = "0.0"

from aws_durable_execution_sdk_python import DurableContext, durable_execution, durable_step
from aws_durable_execution_sdk_python.types import StepContext
from aws_durable_execution_sdk_python.config import Duration
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python.lambda_service import OperationType
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner


@durable_step
def after_wait(ctx: StepContext) -> str:
    return "done"


@durable_execution
def handler(event, context: DurableContext) -> str:
    context.wait(Duration.from_hours(24), name="cooling-off")
    return context.step(after_wait(), name="after-wait")


def test_completes_with_long_wait():
    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        result = runner.run(timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED

    wait_ops = [op for op in result.operations if op.operation_type == OperationType.WAIT]
    assert len(wait_ops) == 1
    assert wait_ops[0].name == "cooling-off"
