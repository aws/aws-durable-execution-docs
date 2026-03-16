import pytest
from aws_durable_execution_sdk_python_testing import InvocationStatus
from my_function import handler

@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="parallel_function",
)
def test_parallel(durable_runner):
    """Test parallel operations."""
    with durable_runner:
        result = durable_runner.run(input={"data": "test"}, timeout=10)
    
    # Check overall status
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Check the result contains expected values
    assert len(result.result) == 3
    assert "Task 1 complete" in result.result
