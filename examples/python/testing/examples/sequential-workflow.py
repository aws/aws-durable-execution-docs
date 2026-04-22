from aws_durable_execution_sdk_python import DurableContext, durable_execution, durable_step
from aws_durable_execution_sdk_python.types import StepContext
from aws_durable_execution_sdk_python.execution import InvocationStatus
from aws_durable_execution_sdk_python.lambda_service import OperationType
from aws_durable_execution_sdk_python_testing.runner import DurableFunctionTestRunner


@durable_step
def validate(ctx: StepContext, order_id: str) -> dict:
    return {"order_id": order_id, "status": "validated"}


@durable_step
def payment(ctx: StepContext, order: dict) -> dict:
    return {**order, "payment": "completed"}


@durable_step
def fulfillment(ctx: StepContext, order: dict) -> dict:
    return {**order, "fulfillment": "shipped"}


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    validated = context.step(validate(event["order_id"]), name="validate")
    paid = context.step(payment(validated), name="payment")
    return context.step(fulfillment(paid), name="fulfillment")


def test_executes_all_steps_in_order():
    runner = DurableFunctionTestRunner(handler=handler)
    with runner:
        result = runner.run(input='{"order_id": "order-123"}', timeout=10)

    assert result.status is InvocationStatus.SUCCEEDED

    step_ops = [op for op in result.operations if op.operation_type == OperationType.STEP]
    assert len(step_ops) == 3
    assert [op.name for op in step_ops] == ["validate", "payment", "fulfillment"]
