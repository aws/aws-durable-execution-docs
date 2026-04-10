from aws_durable_execution_sdk_python.context import DurableContext, StepContext, durable_step
from aws_durable_execution_sdk_python.exceptions import CallableRuntimeError
from aws_durable_execution_sdk_python.execution import durable_execution


@durable_step
def process_order(step_context: StepContext, order_id: str) -> dict:
    if not order_id:
        raise ValueError("order_id is required")
    return {"order_id": order_id, "status": "processed"}


@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    try:
        result = context.step(process_order(event.get("orderId", "")))
        return result
    except CallableRuntimeError as e:
        context.logger.error("Step failed", extra={"error_type": e.error_type})
        return {"error": e.message}
