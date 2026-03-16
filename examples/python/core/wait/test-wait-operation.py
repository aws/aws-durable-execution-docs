import pytest
from aws_durable_execution_sdk_python.execution import InvocationStatus
from src.wait import wait

@pytest.mark.durable_execution(
    handler=wait.handler,
    lambda_function_name="Wait State",
)
def test_wait(durable_runner):
    """Test wait example."""
    with durable_runner:
        result = durable_runner.run(input="test", timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED
    
    # Find the wait operation
    wait_ops = [op for op in result.operations if op.operation_type.value == "WAIT"]
    assert len(wait_ops) == 1
    
    # Verify the wait has a scheduled end timestamp
    wait_op = wait_ops[0]
    assert wait_op.scheduled_end_timestamp is not None
