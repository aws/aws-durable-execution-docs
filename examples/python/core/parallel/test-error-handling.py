@pytest.mark.durable_execution(
    handler=handler_with_failures,
    lambda_function_name="parallel_with_failures",
)
def test_parallel_with_failures(durable_runner):
    """Test parallel operations with some failures."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    # Check that some branches succeeded
    assert result.status is InvocationStatus.SUCCEEDED
    assert result.result["successful_count"] > 0
    assert result.result["failed_count"] > 0
