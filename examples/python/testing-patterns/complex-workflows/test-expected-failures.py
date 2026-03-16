@pytest.mark.durable_execution(handler=handler, lambda_function_name="validation_workflow")
def test_validation_failure(durable_runner):
    """Test validation fails with invalid input."""
    with durable_runner:
        result = durable_runner.run(input={"value": -5}, timeout=30)
    
    assert result.status is InvocationStatus.FAILED
    assert "Value must be non-negative" in str(result.error)
