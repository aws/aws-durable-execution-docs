@pytest.mark.durable_execution(handler=handler, lambda_function_name="retry_workflow")
def test_retry_behavior(durable_runner):
    """Test operation retries on failure."""
    global attempt_count
    attempt_count = 0
    
    with durable_runner:
        result = durable_runner.run(input={}, timeout=60)
    
    assert result.status is InvocationStatus.SUCCEEDED
    assert deserialize_operation_payload(result.result) == "Operation succeeded"
    assert attempt_count >= 3
