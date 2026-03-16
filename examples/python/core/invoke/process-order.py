from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
)

@durable_execution
def process_order(event: dict, context: DurableContext) -> dict:
    """Process an order by validating and charging."""
    order_id = event["order_id"]
    amount = event["amount"]
    
    # Invoke validation function
    validation_result = context.invoke(
        function_name="validate-order",
        payload={"order_id": order_id},
        name="validate_order",
    )
    
    if not validation_result["valid"]:
        return {"status": "rejected", "reason": validation_result["reason"]}
    
    # Invoke payment function
    payment_result = context.invoke(
        function_name="process-payment",
        payload={"order_id": order_id, "amount": amount},
        name="process_payment",
    )
    
    return {
        "status": "completed",
        "order_id": order_id,
        "transaction_id": payment_result["transaction_id"],
    }
