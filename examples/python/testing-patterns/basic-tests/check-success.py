@pytest.mark.durable_execution(
    handler=my_handler,
    lambda_function_name="my_function",
)
def test_success(durable_runner):
    """Test successful execution."""
    with durable_runner:
        result = durable_runner.run(input={"data": "test"}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
