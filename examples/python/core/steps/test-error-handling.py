@pytest.mark.durable_execution(
    handler=handler_with_error,
    lambda_function_name="error_function",
)
def test_step_error(durable_runner):
    """Test step error handling."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    # Function should fail
    assert result.status is InvocationStatus.FAILED
    
    # Check the error
    assert "RuntimeError" in str(result.error)
