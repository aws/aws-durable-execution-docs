import pytest
from aws_durable_execution_sdk_python.execution import InvocationStatus

from my_function import handler_with_error


@pytest.mark.durable_execution(
    handler=handler_with_error,
    lambda_function_name="error_function",
)
def test_step_error(durable_runner):
    with durable_runner:
        result = durable_runner.run(input={}, timeout=10)

    assert result.status is InvocationStatus.FAILED
    assert result.error is not None
