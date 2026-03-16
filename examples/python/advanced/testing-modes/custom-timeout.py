def test_long_running(durable_runner):
    with durable_runner:
        result = durable_runner.run(input="test", timeout=300)  # 5 minutes
    
    assert result.status == InvocationStatus.SUCCEEDED
