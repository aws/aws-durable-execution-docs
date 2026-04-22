from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.types import WaitForCallbackContext
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner


@durable_execution
def handler(event, context: DurableContext) -> str:
    def submit(callback_id: str, ctx: WaitForCallbackContext) -> None:
        pass  # In production this would notify an external system

    return context.wait_for_callback(submit, name="approval")


def test_completes_callback_from_test():
    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        execution_arn = runner.run_async(timeout=10)
        callback_id = runner.wait_for_callback(execution_arn=execution_arn, name="approval", timeout=10)
        runner.send_callback_success(callback_id=callback_id, result=b'"approved"')
        result = runner.wait_for_result(execution_arn=execution_arn, timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED
