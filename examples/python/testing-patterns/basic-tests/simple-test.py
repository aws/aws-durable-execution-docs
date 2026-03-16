import pytest
from aws_durable_execution_sdk_python.execution import InvocationStatus
from test.conftest import deserialize_operation_payload

@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="hello world",
)
def test_hello_world(durable_runner):
    """Test hello world example."""
    with durable_runner:
        result = durable_runner.run(input="test", timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED
    assert deserialize_operation_payload(result.result) == "Hello World!"
