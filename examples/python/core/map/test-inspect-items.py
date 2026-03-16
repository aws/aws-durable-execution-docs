from aws_durable_execution_sdk_python.lambda_service import OperationType

@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="map_operations",
)
def test_map_individual_items(durable_runner):
    """Test individual item processing."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    # Get the map operation
    map_op = result.get_map("square")
    assert map_op is not None
    
    # Verify all items were processed
    assert map_op.result.total_count == 5
    
    # Check specific items
    assert map_op.result.results[0].result == 1
    assert map_op.result.results[2].result == 9
