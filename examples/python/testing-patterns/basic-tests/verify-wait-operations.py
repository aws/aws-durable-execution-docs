@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="wait_function",
)
def test_wait_operation(durable_runner):
    """Test wait operation is created."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Find wait operations
    wait_ops = [
        op for op in result.operations 
        if op.operation_type.value == "WAIT"
    ]
    assert len(wait_ops) == 1
    assert wait_ops[0].scheduled_end_timestamp is not None
