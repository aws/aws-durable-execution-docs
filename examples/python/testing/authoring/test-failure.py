from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner


@durable_execution
def handler(event: dict, context: DurableContext):
    if event.get("fail"):
        raise ValueError("intentional failure")
    return "ok"


def test_reports_failed_execution():
    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        result = runner.run(input='{"fail": true}', timeout=10)

    assert result.status is InvocationStatus.FAILED
    assert result.error is not None
