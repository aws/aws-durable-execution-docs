@pytest.mark.durable_execution(
    handler=handler_with_error_handling,
    lambda_function_name="error_handler_function",
)
def test_invoke_error_handling(durable_runner):
    """Test invoke error handling."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=30)
    
    # Function should handle the error gracefully
    assert result.status is InvocationStatus.SUCCEEDED
    assert result.result["status"] == "failed"
    assert "error" in result.result
