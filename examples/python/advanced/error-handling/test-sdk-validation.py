@pytest.mark.durable_execution(
    handler=handler_with_invalid_config,
    lambda_function_name="sdk_validation_function",
)
def test_sdk_validation_error(durable_runner):
    """Test SDK validation error handling."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    # SDK should catch invalid configuration
    assert result.status is InvocationStatus.FAILED
    assert "ValidationError" in str(result.error)
