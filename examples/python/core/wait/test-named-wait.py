@pytest.mark.durable_execution(handler=wait_with_name.handler)
def test_named_wait(durable_runner):
    """Test wait with custom name."""
    with durable_runner:
        result = durable_runner.run(input="test", timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED
    
    # Find the named wait operation
    wait_ops = [op for op in result.operations 
                if op.operation_type.value == "WAIT" and op.name == "custom_wait"]
    assert len(wait_ops) == 1
