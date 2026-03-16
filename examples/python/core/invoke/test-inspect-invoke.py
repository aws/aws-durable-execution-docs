@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="my_function",
)
def test_invoke_operations(durable_runner):
    """Test and inspect invoke operations."""
    with durable_runner:
        result = durable_runner.run(input={"user_id": "user-123"}, timeout=30)
    
    # Get all operations
    operations = result.operations
    
    # Find invoke operations
    invoke_ops = [op for op in operations if op.operation_type == "CHAINED_INVOKE"]
    
    # Verify invoke operations were created
    assert len(invoke_ops) == 2
    
    # Check specific invoke operation
    validate_op = next(op for op in invoke_ops if op.name == "validate_order")
    assert validate_op.status is InvocationStatus.SUCCEEDED
