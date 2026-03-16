@pytest.mark.durable_execution(
    handler=error_handling_handler,
    lambda_function_name="error_handling",
)
def test_child_context_error_handling(durable_runner):
    """Test error handling in child context."""
    with durable_runner:
        result = durable_runner.run(input={"data": "invalid"}, timeout=10)
    
    # Function should handle error gracefully
    assert result.status is InvocationStatus.SUCCEEDED
    assert result.result["status"] == "fallback"
    assert "error" in result.result
