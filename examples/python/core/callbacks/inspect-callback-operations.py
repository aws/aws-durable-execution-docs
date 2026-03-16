@pytest.mark.durable_execution(
    handler=callback.handler,
    lambda_function_name="callback",
)
def test_callback_operation(durable_runner):
    """Test and inspect callback operation."""
    with durable_runner:
        result = durable_runner.run(input="test", timeout=10)
    
    # Find callback operations
    callback_ops = [
        op for op in result.operations
        if op.operation_type.value == "CALLBACK"
    ]
    
    assert len(callback_ops) == 1
    callback_op = callback_ops[0]
    
    # Verify callback properties
    assert callback_op.name == "example_callback"
    assert callback_op.callback_id is not None
