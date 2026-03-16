# Durable function side
@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Process payment with external payment processor."""
    # Create callback
    callback = context.create_callback(
        name="payment_callback",
        config=CallbackConfig(timeout=Duration.from_minutes(5)),
    )
    
    # Send to message broker (SQS, SNS, EventBridge, etc.)
    send_to_payment_queue({
        "callback_id": callback.callback_id,
        "amount": event["amount"],
        "customer_id": event["customer_id"],
    })
    
    # Wait for result - execution suspends here
    payment_result = callback.result()
    
    # Execution resumes here when callback is notified
    return {
        "payment_status": payment_result.get("status"),
        "transaction_id": payment_result.get("transaction_id"),
    }
