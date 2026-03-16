@pytest.mark.durable_execution(
    handler=handler_with_validation,
    lambda_function_name="validation_function",
)
def test_input_validation(durable_runner):
    """Test input validation handling."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    # Function should return error response for invalid input
    assert result.status is InvocationStatus.SUCCEEDED
    assert "error" in result.result
    assert result.result["error"] == "InvalidInput"
