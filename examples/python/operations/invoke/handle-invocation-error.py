from aws_durable_execution_sdk_python import DurableContext, durable_execution
from aws_durable_execution_sdk_python.exceptions import CallableRuntimeError


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    try:
        result = context.invoke(
            "payment-processor-function:live",
            {"order_id": event["order_id"]},
            name="process-payment",
        )
        return {"status": "success", "result": result}
    except CallableRuntimeError as e:
        return {"status": "failed", "reason": str(e)}
