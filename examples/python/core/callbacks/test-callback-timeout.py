from examples.src.callback import callback_with_timeout

@pytest.mark.durable_execution(
    handler=callback_with_timeout.handler,
    lambda_function_name="callback_timeout",
)
def test_callback_timeout(durable_runner):
    """Test callback with custom timeout."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
    assert "60s timeout" in result.result
