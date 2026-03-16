@pytest.mark.parametrize("a,b,expected", [
    (5, 3, 8),
    (10, 20, 30),
    (0, 0, 0),
    (-5, 5, 0),
])
@pytest.mark.durable_execution(handler=add_handler, lambda_function_name="calculator")
def test_addition(durable_runner, a, b, expected):
    """Test addition with various inputs."""
    with durable_runner:
        result = durable_runner.run(input={"a": a, "b": b}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
    assert deserialize_operation_payload(result.result) == expected
