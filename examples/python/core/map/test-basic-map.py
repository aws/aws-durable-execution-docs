import pytest
from aws_durable_execution_sdk_python_testing import InvocationStatus
from my_function import handler

@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="map_operations",
)
def test_map_operations(durable_runner):
    """Test map operations."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    # Check overall status
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Check the BatchResult
    batch_result = result.result
    assert batch_result.total_count == 5
    assert batch_result.success_count == 5
    assert batch_result.failure_count == 0
    
    # Check individual results
    assert batch_result.results[0].result == 1
    assert batch_result.results[1].result == 4
    assert batch_result.results[2].result == 9
