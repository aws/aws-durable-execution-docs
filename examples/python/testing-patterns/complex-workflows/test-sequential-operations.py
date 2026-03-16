import pytest
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python.lambda_service import OperationType
from test.conftest import deserialize_operation_payload

@pytest.mark.durable_execution(handler=handler, lambda_function_name="order_workflow")
def test_order_workflow(durable_runner):
    """Test order processing executes all steps."""
    with durable_runner:
        result = durable_runner.run(input={"order_id": "order-123"}, timeout=30)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Check final result
    final_result = deserialize_operation_payload(result.result)
    assert final_result["order_id"] == "order-123"
    assert final_result["payment_status"] == "completed"
    assert final_result["fulfillment_status"] == "shipped"
    
    # Verify all three steps ran
    step_ops = [op for op in result.operations if op.operation_type == OperationType.STEP]
    assert len(step_ops) == 3
    
    # Check step order
    step_names = [op.name for op in step_ops]
    assert step_names == ["validate", "payment", "fulfillment"]
