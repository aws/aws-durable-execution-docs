@pytest.mark.durable_execution(handler=handler, lambda_function_name="polling")
def test_polling(durable_runner):
    """Test wait-for-condition pattern."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=30)
    
    assert result.status is InvocationStatus.SUCCEEDED
    assert deserialize_operation_payload(result.result) == 3
    
    # Should have 3 increment steps
    step_ops = [op for op in result.operations if op.operation_type == OperationType.STEP]
    assert len(step_ops) == 3
    
    # Should have 2 waits (before reaching state 3)
    wait_ops = [op for op in result.operations if op.operation_type.value == "WAIT"]
    assert len(wait_ops) == 2
