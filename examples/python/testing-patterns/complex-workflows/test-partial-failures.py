@pytest.mark.durable_execution(handler=handler, lambda_function_name="partial_failure")
def test_partial_failure(durable_runner):
    """Test workflow fails after some steps succeed."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=30)
    
    assert result.status is InvocationStatus.FAILED
    
    # First two steps succeeded
    step1 = result.get_step("step1")
    assert deserialize_operation_payload(step1.result) == "Step 1 complete"
    
    step2 = result.get_step("step2")
    assert deserialize_operation_payload(step2.result) == "Step 2 complete"
    
    assert "Step 3 failed" in str(result.error)
