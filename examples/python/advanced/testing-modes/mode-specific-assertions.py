def test_with_mode_check(durable_runner):
    with durable_runner:
        result = durable_runner.run(input="test", timeout=10)
    
    assert result.status == InvocationStatus.SUCCEEDED
    
    # Cloud-specific validation
    if durable_runner.mode == "cloud":
        # Additional assertions for cloud environment
        pass
