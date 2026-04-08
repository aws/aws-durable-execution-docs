from examples.src.run_in_child_context import run_in_child_context_large_data

@pytest.mark.durable_execution(
    handler=run_in_child_context_large_data.handler,
    lambda_function_name="run in child context large data",
)
def test_large_data_processing(durable_runner):
    """Test large data handling with child context."""
    with durable_runner:
        result = durable_runner.run(input=None, timeout=30)
    
    result_data = result.result
    
    # Verify execution succeeded
    assert result.status is InvocationStatus.SUCCEEDED
    assert result_data["success"] is True
    
    # Verify large data was processed
    assert result_data["summary"]["totalDataSize"] > 240  # ~250KB
    assert result_data["summary"]["stepsExecuted"] == 5
    
    # Verify data integrity across wait
    assert result_data["dataIntegrityCheck"] is True
