import pytest
from aws_durable_execution_sdk_python.execution import InvocationStatus
from test.conftest import deserialize_operation_payload

@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="step_function",
)
def test_step_execution(durable_runner):
    """Test step executes correctly."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Get step by name
    step_result = result.get_step("add_numbers")
    assert deserialize_operation_payload(step_result.result) == 8
