@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="callback_function",
)
def test_callback_creation(durable_runner):
    """Test callback is created correctly."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Find callback operations
    callback_ops = [
        op for op in result.operations 
        if op.operation_type.value == "CALLBACK"
    ]
    assert len(callback_ops) == 1
    
    callback_op = callback_ops[0]
    assert callback_op.name == "example_callback"
    assert callback_op.callback_id is not None
