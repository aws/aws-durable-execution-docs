@pytest.mark.durable_execution(
    handler=approval_workflow_handler,
    lambda_function_name="approval_workflow",
)
def test_approval_workflow(durable_runner):
    """Test approval workflow with callback."""
    with durable_runner:
        result = durable_runner.run(
            input={"order_id": "order-123", "amount": 1000},
            timeout=30,
        )
    
    # Verify workflow completed
    assert result.status is InvocationStatus.SUCCEEDED
    
    # Check callback was created
    callback_ops = [
        op for op in result.operations
        if op.operation_type.value == "CALLBACK"
    ]
    assert len(callback_ops) == 1
    assert callback_ops[0].name == "order_approval"
