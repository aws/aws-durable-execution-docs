@pytest.mark.durable_execution(handler=handler, lambda_function_name="parallel_ops")
def test_parallel_operations(durable_runner):
    """Test parallel execution."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=30)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    results = deserialize_operation_payload(result.result)
    assert len(results) == 3
    
    # Verify all steps ran
    step_ops = [op for op in result.operations if op.operation_type == OperationType.STEP]
    assert len(step_ops) == 3
    
    step_names = {op.name for op in step_ops}
    assert step_names == {"task1", "task2", "task3"}
