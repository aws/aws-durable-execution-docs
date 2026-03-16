@pytest.mark.durable_execution(
    handler=handler_with_errors,
    lambda_function_name="map_with_errors",
)
def test_map_error_handling(durable_runner):
    """Test error handling in map operations."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    # Function should handle errors based on completion config
    assert result.status is InvocationStatus.SUCCEEDED
    
    batch_result = result.result
    
    # Check that some items succeeded
    successful = [r for r in batch_result.results if r.status == "SUCCEEDED"]
    assert len(successful) > 0
    
    # Check that some items failed
    failed = [r for r in batch_result.results if r.status == "FAILED"]
    assert len(failed) > 0
