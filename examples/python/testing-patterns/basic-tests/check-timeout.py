@pytest.mark.durable_execution(
    handler=quick_handler,
    lambda_function_name="quick_function",
)
def test_completes_quickly(durable_runner):
    """Test that function completes within timeout."""
    with durable_runner:
        # Use a short timeout to verify quick execution
        result = durable_runner.run(input={}, timeout=5)
    
    assert result.status is InvocationStatus.SUCCEEDED
