from test.conftest import deserialize_operation_payload

@pytest.mark.durable_execution(
    handler=calculator_handler,
    lambda_function_name="calculator",
)
def test_calculation_result(durable_runner):
    """Test calculation returns correct result."""
    with durable_runner:
        result = durable_runner.run(input={"a": 5, "b": 3}, timeout=10)
    
    assert result.status is InvocationStatus.SUCCEEDED
    assert deserialize_operation_payload(result.result) == 8
