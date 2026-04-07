from aws_durable_execution_sdk_python import DurableContext, durable_execution


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    validation = context.invoke(
        "validate-order-function:live",
        {"order_id": event["order_id"], "amount": event["amount"]},
        name="validate-order",
    )

    if not validation["valid"]:
        return {"status": "rejected", "reason": validation.get("reason")}

    payment = context.invoke(
        "payment-processor-function:live",
        {"order_id": event["order_id"], "amount": event["amount"]},
        name="process-payment",
    )

    return {"status": "completed", "transaction_id": payment["transaction_id"]}
