@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="my_function",
)
def test_step_result(durable_runner):
    """Test and inspect step results."""
    with durable_runner:
        result = durable_runner.run(input={"data": "test"}, timeout=10)
    
    # Get step by name
    step_result = result.get_step("add_numbers")
    assert step_result.result == 8
    
    # Check step status
    assert step_result.status is InvocationStatus.SUCCEEDED
