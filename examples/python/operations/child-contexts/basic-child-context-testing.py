import pytest
from aws_durable_execution_sdk_python_testing import InvocationStatus
from examples.src.run_in_child_context import run_in_child_context

@pytest.mark.durable_execution(
    handler=run_in_child_context.handler,
    lambda_function_name="run in child context",
)
def test_run_in_child_context(durable_runner):
    """Test basic child context execution."""
    with durable_runner:
        result = durable_runner.run(input="test", timeout=10)
    
    # Check overall status
    assert result.status is InvocationStatus.SUCCEEDED
    assert result.result == "Child context result: 10"
