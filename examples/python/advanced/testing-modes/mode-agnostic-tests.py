@pytest.mark.durable_execution(
    handler=my_function.handler,
    lambda_function_name="my function",
)
def test_my_function(durable_runner):
    """Test works in both local and cloud modes."""
    with durable_runner:
        result = durable_runner.run(input={"value": 42}, timeout=10)
    
    # These assertions work in both modes
    assert result.status == InvocationStatus.SUCCEEDED
    assert result.result == "expected output"
