from aws_durable_execution_sdk_python import DurableContext, InvokeError, durable_execution


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    try:
        result = context.invoke(
            "payment-processor-function:live",
            {"order_id": event["order_id"]},
            name="process-payment",
        )
        return {"status": "success", "result": result}
    except InvokeError as e:
        return {"status": "failed", "reason": str(e)}
