@pytest.mark.durable_execution(
    handler=parallel_handler,
    lambda_function_name="parallel_tasks",
)
def test_parallel_results(durable_runner):
    """Test parallel operations return all results."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    results = deserialize_operation_payload(result.result)
    assert len(results) == 3
    assert results == [
        "Task 1 complete",
        "Task 2 complete",
        "Task 3 complete",
    ]
