@pytest.mark.durable_execution(
    handler=handler_with_error,
    lambda_function_name="error_function",
)
def test_error_details(durable_runner):
    """Test error details are captured."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    # Check error details
    assert result.status is InvocationStatus.FAILED
    assert result.error is not None
    assert "error_type" in result.error
    assert "message" in result.error
