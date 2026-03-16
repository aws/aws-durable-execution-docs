import pytest
from aws_durable_execution_sdk_python.execution import InvocationStatus

from src.logger_example import logger_example
from test.conftest import deserialize_operation_payload

@pytest.mark.durable_execution(
    handler=logger_example.handler,
    lambda_function_name="logger example",
)
def test_logger_example(durable_runner):
    """Test logger example."""
    with durable_runner:
        result = durable_runner.run(input={"id": "test-123"}, timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED
    assert deserialize_operation_payload(result.result) == "processed-child-processed"
