@pytest.mark.durable_execution(handler=multiple_wait.handler)
def test_multiple_waits(durable_runner):
    """Test multiple sequential waits."""
    with durable_runner:
        result = durable_runner.run(input="test", timeout=20)

    assert result.status is InvocationStatus.SUCCEEDED
    
    # Find all wait operations
    wait_ops = [op for op in result.operations if op.operation_type.value == "WAIT"]
    assert len(wait_ops) == 2
    
    # Verify both waits have names
    wait_names = [op.name for op in wait_ops]
    assert "wait-1" in wait_names
    assert "wait-2" in wait_names
