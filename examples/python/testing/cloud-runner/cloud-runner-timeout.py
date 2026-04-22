from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionCloudTestRunner


def test_runs_with_custom_timeout():
    runner = DurableFunctionCloudTestRunner(
        function_name="MyFunction:$LATEST",
        region="us-east-1",
    )
    result = runner.run(input='{"name": "world"}', timeout=60)

    assert result.status is InvocationStatus.SUCCEEDED
