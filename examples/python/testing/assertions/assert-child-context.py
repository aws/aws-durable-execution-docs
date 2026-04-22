from aws_durable_execution_sdk_python import DurableContext, durable_execution, durable_step
from aws_durable_execution_sdk_python.types import StepContext
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python.lambda_service import OperationType
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner


@durable_step
def compute(ctx: StepContext) -> int:
    return 42


@durable_execution
def handler(event, context: DurableContext) -> int:
    def process(child: DurableContext) -> int:
        return child.step(compute())

    return context.run_in_child_context(process, name="process")


def test_asserts_on_child_context():
    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        result = runner.run(timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED

    ctx_ops = [op for op in result.operations if op.operation_type == OperationType.CONTEXT]
    assert len(ctx_ops) == 1
    assert ctx_ops[0].name == "process"
