@pytest.mark.durable_execution(
    handler=run_in_child_context.handler,
    lambda_function_name="run in child context",
)
def test_child_context_operations(durable_runner):
    """Test and inspect child context operations."""
    with durable_runner:
        result = durable_runner.run(input="test", timeout=10)
    
    # Verify child context operation exists
    context_ops = [
        op for op in result.operations
        if op.operation_type.value == "CONTEXT"
    ]
    assert len(context_ops) >= 1
    
    # Get child context by name (if named)
    child_result = result.get_context("child_operation")
    assert child_result is not None
