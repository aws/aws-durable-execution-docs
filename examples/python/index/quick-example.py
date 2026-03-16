from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_step,
)
from aws_durable_execution_sdk_python.config import Duration


@durable_step
def validate_order(order_id: str) -> dict:
    # Validation logic here
    return {"order_id": order_id, "valid": True}


@durable_step
def charge_payment(order_id: str, amount: float) -> dict:
    # Payment processing logic here
    return {"transaction_id": "txn_123", "status": "completed"}


@durable_step
def fulfill_order(order_id: str) -> dict:
    # Fulfillment logic here
    return {"tracking_number": "TRK123456"}


@durable_execution
def process_order(event: dict, context: DurableContext) -> dict:
    order_id = event["order_id"]
    amount = event["amount"]

    # Step 1: Validate the order
    validation = context.step(validate_order(order_id))

    if not validation["valid"]:
        return {"status": "failed", "reason": "invalid_order"}

    # Step 2: Charge payment
    payment = context.step(charge_payment(order_id, amount))

    # Step 3: Wait for payment confirmation (simulated)
    context.wait(Duration.from_seconds(5))

    # Step 4: Fulfill the order
    fulfillment = context.step(fulfill_order(order_id))

    return {
        "status": "completed",
        "order_id": order_id,
        "transaction_id": payment["transaction_id"],
        "tracking_number": fulfillment["tracking_number"],
    }
