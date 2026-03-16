@pytest.mark.durable_execution(handler=handler, lambda_function_name="multiple_contexts")
def test_multiple_child_contexts(durable_runner):
    """Test multiple child contexts."""
    with durable_runner:
        result = durable_runner.run(input={"data": {"value": 42}}, timeout=30)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    final_result = deserialize_operation_payload(result.result)
    assert final_result["validated"] is True
    assert final_result["transformed"] is True
    
    # Verify both contexts ran
    context_ops = [op for op in result.operations if op.operation_type.value == "CONTEXT"]
    assert len(context_ops) == 2
