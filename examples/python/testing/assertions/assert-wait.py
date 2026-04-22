from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.config import Duration
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python.lambda_service import OperationStatus, OperationType
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner


@durable_execution
def handler(event, context: DurableContext) -> str:
    context.wait(Duration.from_seconds(30), name="my-wait")
    return "done"


def test_asserts_on_wait_operation():
    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        result = runner.run(timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED

    wait = result.get_wait("my-wait")
    assert wait.status is OperationStatus.SUCCEEDED
    assert wait.scheduled_end_timestamp is not None
