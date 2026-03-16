@pytest.mark.durable_execution(
    handler=my_function.handler,           # Required for local mode
    lambda_function_name="my function",    # Required for cloud mode
)
def test_my_function(durable_runner):
    # Test code here
    pass
