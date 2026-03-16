from aws_durable_execution_sdk_python.lambda_service import OperationType

@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="parallel_function",
)
def test_parallel_operations(durable_runner):
    """Test parallel operations execute."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Find all step operations
    step_ops = [
        op for op in result.operations 
        if op.operation_type == OperationType.STEP
    ]
    assert len(step_ops) == 3
    
    # Verify step names
    step_names = {op.name for op in step_ops}
    assert step_names == {"task1", "task2", "task3"}
