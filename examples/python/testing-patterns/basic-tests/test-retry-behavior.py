import pytest
from aws_durable_execution_sdk_python.execution import InvocationStatus

from my_function import handler_with_retry


@pytest.mark.durable_execution(
    handler=handler_with_retry,
    lambda_function_name="retry_function",
)
def test_step_retry(durable_runner):
    with durable_runner:
        result = durable_runner.run(input={}, timeout=30)

    assert result.status is InvocationStatus.SUCCEEDED

    step_result = result.get_step("unreliable_operation")
    assert step_result.status is InvocationStatus.SUCCEEDED
