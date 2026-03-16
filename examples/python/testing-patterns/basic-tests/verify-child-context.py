@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="child_context_function",
)
def test_child_context(durable_runner):
    """Test child context executes."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Find child context operations
    context_ops = [
        op for op in result.operations 
        if op.operation_type.value == "CONTEXT"
    ]
    assert len(context_ops) >= 1
