@pytest.mark.durable_execution(handler=handler, lambda_function_name="conditional_workflow")
def test_high_value_path(durable_runner):
    """Test high-value orders require approval."""
    with durable_runner:
        result = durable_runner.run(input={"amount": 1500}, timeout=30)
    
    assert result.status is InvocationStatus.SUCCEEDED
    assert deserialize_operation_payload(result.result) == "High-value order processed"
    
    # Verify approval step exists
    approval_step = result.get_step("approval")
    assert approval_step is not None

@pytest.mark.durable_execution(handler=handler, lambda_function_name="conditional_workflow")
def test_standard_path(durable_runner):
    """Test standard orders skip approval."""
    with durable_runner:
        result = durable_runner.run(input={"amount": 500}, timeout=30)
    
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Verify no approval step
    step_names = [op.name for op in result.operations if op.operation_type == OperationType.STEP]
    assert "approval" not in step_names
