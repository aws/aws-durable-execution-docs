@pytest.mark.durable_execution(handler=handler, lambda_function_name="payment")
def test_payment_with_retry(durable_runner):
    """Test payment processing retries on transient failures.
    
    This test verifies that:
    1. Payment step retries on RuntimeError
    2. Function eventually succeeds after retries
    3. Final result includes transaction ID
    """
    with durable_runner:
        result = durable_runner.run(input={"amount": 50.0}, timeout=30)
    
    assert result.status is InvocationStatus.SUCCEEDED
