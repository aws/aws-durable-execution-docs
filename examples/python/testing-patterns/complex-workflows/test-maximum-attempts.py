@pytest.mark.durable_execution(handler=handler, lambda_function_name="max_attempts")
def test_max_attempts(durable_runner):
    """Test polling stops at max attempts."""
    with durable_runner:
        result = durable_runner.run(input={"target": 10}, timeout=30)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    final_result = deserialize_operation_payload(result.result)
    assert final_result["attempts"] == 5
    assert final_result["state"] == 5
    assert final_result["reached_target"] is False
