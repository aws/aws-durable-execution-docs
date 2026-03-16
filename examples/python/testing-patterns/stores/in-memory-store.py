import pytest
from aws_durable_execution_sdk_python.execution import InvocationStatus

@pytest.mark.durable_execution(
    handler=my_handler,
    lambda_function_name="my_function",
)
def test_with_memory_store(durable_runner):
    """Test uses in-memory store by default."""
    with durable_runner:
        result = durable_runner.run(input={"data": "test"}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
