@pytest.mark.durable_execution(
    handler=handler_with_concurrency_limit,
    lambda_function_name="limited_concurrency",
)
def test_concurrency_limit(durable_runner):
    """Test parallel operations with concurrency limit."""
    with durable_runner:
        result = durable_runner.run(input={"items": list(range(20))}, timeout=30)
    
    # All items should be processed
    assert result.status is InvocationStatus.SUCCEEDED
    assert len(result.result["results"]) == 20
