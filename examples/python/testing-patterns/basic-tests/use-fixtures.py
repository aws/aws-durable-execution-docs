# conftest.py
@pytest.fixture
def valid_order():
    """Provide valid order data."""
    return {
        "order_id": "order-123",
        "customer_id": "customer-456",
        "amount": 100.0,
        "items": [
            {"product_id": "prod-1", "quantity": 2},
            {"product_id": "prod-2", "quantity": 1},
        ],
    }

# test_orders.py
@pytest.mark.durable_execution(handler=handler, lambda_function_name="orders")
def test_order_processing(durable_runner, valid_order):
    """Test order processing with valid data."""
    with durable_runner:
        result = durable_runner.run(input=valid_order, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
