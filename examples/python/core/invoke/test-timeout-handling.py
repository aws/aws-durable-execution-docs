from aws_durable_execution_sdk_python.config import Duration, InvokeConfig

@pytest.mark.durable_execution(
    handler=handler_with_timeout,
    lambda_function_name="timeout_function",
)
def test_invoke_timeout(durable_runner):
    """Test invoke timeout handling."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=60)
    
    # Check that timeout was handled
    assert result.status is InvocationStatus.SUCCEEDED
    assert result.result["status"] == "timeout"
