@pytest.mark.durable_execution(handler=handler, lambda_function_name="long_wait")
def test_long_wait(durable_runner):
    """Test long wait configuration."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Verify wait exists
    wait_ops = [op for op in result.operations if op.operation_type.value == "WAIT"]
    assert len(wait_ops) == 1
    assert wait_ops[0].name == "long_wait"
