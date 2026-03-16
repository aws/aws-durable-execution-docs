import pytest
from aws_durable_execution_sdk_python.execution import InvocationStatus
from my_function import handler

@pytest.mark.durable_execution(handler=handler, lambda_function_name="my_function")
def test_my_function(durable_runner):
    with durable_runner:
        result = durable_runner.run(input={"data": "test"}, timeout=10)
    assert result.status == InvocationStatus.SUCCEEDED
