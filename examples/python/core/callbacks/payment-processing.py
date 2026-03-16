@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Process a payment with external processor."""
    amount = event["amount"]
    customer_id = event["customer_id"]
    
    # Create callback for payment result
    payment_callback = context.create_callback(
        name="payment_processing",
        config=CallbackConfig(
            timeout=Duration.from_minutes(5),
            heartbeat_timeout=Duration.from_seconds(30),
        ),
    )
    
    # Initiate payment with external processor
    initiate_payment_with_processor({
        "callback_id": payment_callback.callback_id,
        "amount": amount,
        "customer_id": customer_id,
        "callback_url": f"https://api.example.com/callbacks/{payment_callback.callback_id}",
    })
    
    # Wait for payment result
    payment_result = payment_callback.result()
    
    return {
        "transaction_id": payment_result.get("transaction_id"),
        "status": payment_result.get("status"),
        "amount": amount,
    }
