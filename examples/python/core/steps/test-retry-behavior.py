@pytest.mark.durable_execution(
    handler=handler_with_retry,
    lambda_function_name="retry_function",
)
def test_step_retry(durable_runner):
    """Test step retry behavior."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=30)
    
    # Function should eventually succeed after retries
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Inspect the step that retried
    step_result = result.get_step("unreliable_operation")
    assert step_result.status is InvocationStatus.SUCCEEDED
