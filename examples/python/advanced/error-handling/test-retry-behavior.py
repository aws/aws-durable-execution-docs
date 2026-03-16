@pytest.mark.durable_execution(
    handler=handler_with_retry,
    lambda_function_name="retry_function",
)
def test_retry_success(durable_runner):
    """Test that retries eventually succeed."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=30)
    
    # Should succeed after retries
    assert result.status is InvocationStatus.SUCCEEDED
