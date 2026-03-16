import logging
import pytest
from aws_durable_execution_sdk_python.execution import InvocationStatus

@pytest.mark.durable_execution(handler=my_handler)
def test_logging_output(durable_runner, caplog):
    """Test that expected log messages are emitted."""
    with caplog.at_level(logging.INFO):
        with durable_runner:
            result = durable_runner.run(input={"id": "test-123"}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Verify log messages
    assert "Starting workflow" in caplog.text
    assert "Workflow completed" in caplog.text
