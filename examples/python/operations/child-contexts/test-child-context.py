import pytest
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python_testing.model import OperationType

from examples.python.core.child_contexts.basic_child_context import handler


@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="basic-child-context",
)
def test_child_context_succeeds(durable_runner):
    with durable_runner:
        result = durable_runner.run(input={"order_id": "order-1"}, timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED

    context_ops = [op for op in result.operations if op.operation_type is OperationType.CONTEXT]
    assert len(context_ops) >= 1
