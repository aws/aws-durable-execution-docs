# Good - focused on one behavior
@pytest.mark.durable_execution(handler=handler, lambda_function_name="orders")
def test_order_validation_succeeds(durable_runner):
    """Test order validation with valid input."""
    with durable_runner:
        result = durable_runner.run(input={"order_id": "order-123"}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED

@pytest.mark.durable_execution(handler=handler, lambda_function_name="orders")
def test_order_validation_fails_missing_id(durable_runner):
    """Test order validation fails without order ID."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    assert result.status is InvocationStatus.FAILED

# Avoid - testing multiple behaviors
@pytest.mark.durable_execution(handler=handler, lambda_function_name="orders")
def test_order_validation(durable_runner):
    """Test order validation."""
    # Test valid input
    result1 = durable_runner.run(input={"order_id": "order-123"}, timeout=10)
    assert result1.status is InvocationStatus.SUCCEEDED
    
    # Test invalid input
    result2 = durable_runner.run(input={}, timeout=10)
    assert result2.status is InvocationStatus.FAILED
