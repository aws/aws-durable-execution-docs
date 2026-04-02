import pytest
from aws_durable_execution_sdk_python.execution import InvocationStatus

from my_function import handler


@pytest.mark.durable_execution(
    handler=handler,
    lambda_function_name="my_function",
)
def test_step_result(durable_runner):
    with durable_runner:
        result = durable_runner.run(input={"data": "test"}, timeout=10)

    step_result = result.get_step("add_numbers")
    assert step_result.status is InvocationStatus.SUCCEEDED
