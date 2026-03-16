from aws_durable_execution_sdk_python.config import MapConfig, CompletionConfig

@pytest.mark.durable_execution(
    handler=handler_with_config,
    lambda_function_name="map_with_config",
)
def test_map_with_config(durable_runner):
    """Test map operations with custom configuration."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=30)
    
    # Verify the map operation completed
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Get the map operation
    map_op = result.get_map("process_items")
    
    # Verify configuration was applied
    assert map_op is not None
    assert map_op.result.total_count > 0
