@pytest.mark.durable_execution(handler=multiple_wait.handler)
def test_multiple_waits(durable_runner):
    """Test multiple sequential waits."""
    with durable_runner:
        result = durable_runner.run(input="test", timeout=20)

    assert result.status is InvocationStatus.SUCCEEDED
    
    # Find wait operations by name
    wait_1 = result.get_wait("wait-1")
    wait_2 = result.get_wait("wait-2")
    
    assert wait_1.scheduled_end_timestamp is not None
    assert wait_2.scheduled_end_timestamp is not None
