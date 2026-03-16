@pytest.mark.durable_execution(handler=handler, lambda_function_name="callback_timeout")
def test_callback_timeout(durable_runner):
    """Test callback timeout configuration."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    callback_ops = [op for op in result.operations if op.operation_type.value == "CALLBACK"]
    assert len(callback_ops) == 1
    assert callback_ops[0].name == "approval_callback"
