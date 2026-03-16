@pytest.mark.durable_execution(handler=handler, lambda_function_name="child_context_workflow")
def test_child_context(durable_runner):
    """Test child context execution."""
    with durable_runner:
        result = durable_runner.run(input={"item_id": "item-123"}, timeout=30)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Check child context ran
    context_ops = [op for op in result.operations if op.operation_type.value == "CONTEXT"]
    assert len(context_ops) == 1
    assert context_ops[0].name == "item_processing"
    
    # Check child context result
    child_result = result.get_context("item_processing")
    child_data = deserialize_operation_payload(child_result.result)
    assert child_data["item_id"] == "item-123"
