@pytest.mark.durable_execution(
    handler=handler_always_fails,
    lambda_function_name="failing_function",
)
def test_retry_exhausted(durable_runner):
    """Test that execution fails after exhausting retries."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=30)
    
    # Should fail after all retries
    assert result.status is InvocationStatus.FAILED
    assert "RuntimeError" in str(result.error)
