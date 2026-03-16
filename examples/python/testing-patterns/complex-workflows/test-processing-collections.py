@pytest.mark.durable_execution(handler=handler, lambda_function_name="parallel_collection")
def test_collection_processing(durable_runner):
    """Test collection processing."""
    with durable_runner:
        result = durable_runner.run(input={"numbers": [1, 2, 3, 4, 5]}, timeout=30)
    
    assert result.status is InvocationStatus.SUCCEEDED
    assert deserialize_operation_payload(result.result) == [2, 4, 6, 8, 10]
    
    # Verify all steps ran
    step_ops = [op for op in result.operations if op.operation_type == OperationType.STEP]
    assert len(step_ops) == 5
