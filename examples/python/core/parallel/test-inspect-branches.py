from aws_durable_execution_sdk_python_testing import OperationType

@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="parallel_function",
)
def test_parallel_branches(durable_runner):
    """Test and inspect parallel branches."""
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)
    
    # Verify all step operations exist
    step_ops = [
        op for op in result.operations
        if op.operation_type == OperationType.STEP
    ]
    assert len(step_ops) == 3
    
    # Check step names
    step_names = {op.name for op in step_ops}
    assert step_names == {"task1", "task2", "task3"}
