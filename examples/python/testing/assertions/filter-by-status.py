from aws_durable_execution_sdk_python import DurableContext, durable_execution, durable_step
from aws_durable_execution_sdk_python.types import StepContext
from aws_durable_execution_sdk_python.config import StepConfig
from aws_durable_execution_sdk_python.retries import create_retry_strategy, RetryStrategyConfig
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python.lambda_service import OperationStatus
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner

call_count = 0


@durable_step
def flaky(ctx: StepContext) -> str:
    global call_count
    call_count += 1
    if call_count < 3:
        raise RuntimeError("not yet")
    return "ok"


@durable_execution
def handler(event, context: DurableContext) -> str:
    config = StepConfig(
        retry_strategy=create_retry_strategy(RetryStrategyConfig(max_attempts=3))
    )
    return context.step(flaky(), config=config)


def test_filters_operations_by_status():
    global call_count
    call_count = 0

    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        result = runner.run(timeout=30)

    assert result.status is InvocationStatus.SUCCEEDED

    failed_ops = [op for op in result.get_all_operations()
                  if op.status is OperationStatus.FAILED]
    assert len(failed_ops) == 2

    succeeded_ops = [op for op in result.get_all_operations()
                     if op.status is OperationStatus.SUCCEEDED]
    assert len(succeeded_ops) == 1
