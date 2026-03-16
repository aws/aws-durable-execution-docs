@pytest.mark.durable_execution(
    handler=order_handler,
    lambda_function_name="order_processor",
)
def test_order_processing(durable_runner):
    """Test order processing returns correct structure."""
    with durable_runner:
        result = durable_runner.run(
            input={"order_id": "order-123", "amount": 100.0},
            timeout=10
        )
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    order_result = deserialize_operation_payload(result.result)
    assert order_result["order_id"] == "order-123"
    assert order_result["status"] == "completed"
    assert order_result["amount"] == 100.0
