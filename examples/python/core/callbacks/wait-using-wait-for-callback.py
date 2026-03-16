@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Create callback
    callback = context.create_callback(name="payment_callback")
    
    # Send to payment processor
    initiate_payment(callback.callback_id, event["amount"])
    
    # Wait for payment result
    payment_result = context.wait_for_callback(
        callback.callback_id,
        config=CallbackConfig(timeout=Duration.from_minutes(5)),
    )
    
    return {"payment_status": payment_result}
