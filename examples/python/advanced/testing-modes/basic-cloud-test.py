import pytest
from aws_durable_execution_sdk_python.execution import InvocationStatus
from examples.src import hello_world


@pytest.mark.durable_execution(
    handler=hello_world.handler,
    lambda_function_name="hello world",
)
def test_hello_world(durable_runner):
    """Test hello world in both local and cloud modes."""
    with durable_runner:
        result = durable_runner.run(input="test", timeout=10)
    
    assert result.status == InvocationStatus.SUCCEEDED
    assert result.result == "Hello World!"
