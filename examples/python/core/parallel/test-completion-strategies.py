@pytest.mark.durable_execution(
    handler=handler_first_successful,
    lambda_function_name="first_successful_function",
)
def test_first_successful(durable_runner):
    """Test first successful completion strategy."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    # Should succeed with at least one result
    assert result.status is InvocationStatus.SUCCEEDED
    assert "First successful result:" in result.result
