from unittest.mock import Mock, patch

@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="my_function",
)
def test_invoke_with_mock(durable_runner):
    """Test invoke with mocked function."""
    # The testing framework handles invocations internally
    # You can test the orchestration logic without deploying all functions
    
    with durable_runner:
        result = durable_runner.run(
            input={"order_id": "order-123"},
            timeout=30,
        )
    
    # Verify the orchestration logic
    assert result.status is InvocationStatus.SUCCEEDED
