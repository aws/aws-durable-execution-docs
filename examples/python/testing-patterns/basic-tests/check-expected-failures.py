@pytest.mark.durable_execution(
    handler=handler_with_validation,
    lambda_function_name="validation_function",
)
def test_validation_failure(durable_runner):
    """Test that invalid input causes failure."""
    with durable_runner:
        result = durable_runner.run(input={"invalid": "data"}, timeout=10)
    
    assert result.status is InvocationStatus.FAILED
    assert "ValidationError" in str(result.error)
