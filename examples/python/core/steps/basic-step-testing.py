import pytest
from aws_durable_execution_sdk_python_testing import InvocationStatus
from my_function import handler

@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="my_function",
)
def test_step(durable_runner):
    """Test a function with steps."""
    with durable_runner:
        result = durable_runner.run(input={"data": "test"}, timeout=10)
    
    # Check overall status
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Check final result
    assert result.result == 8
