import pytest
from aws_durable_execution_sdk_python_testing import InvocationStatus
from examples.src.callback import callback

@pytest.mark.durable_execution(
    handler=callback.handler,
    lambda_function_name="callback",
)
def test_callback(durable_runner):
    """Test callback creation."""
    with durable_runner:
        result = durable_runner.run(input="test", timeout=10)
    
    # Check overall status
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Verify callback was created
    assert "Callback created with ID:" in result.result
