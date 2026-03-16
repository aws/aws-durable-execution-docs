@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Wait for multiple external systems."""
    # Create callbacks for different systems
    credit_check = context.create_callback(
        name="credit_check",
        config=CallbackConfig(timeout=Duration.from_minutes(5)),
    )
    
    fraud_check = context.create_callback(
        name="fraud_check",
        config=CallbackConfig(timeout=Duration.from_minutes(3)),
    )
    
    # Send requests to external systems
    request_credit_check(credit_check.callback_id, event["customer_id"])
    request_fraud_check(fraud_check.callback_id, event["transaction_data"])
    
    # Wait for both results
    credit_result = credit_check.result()
    fraud_result = fraud_check.result()
    
    # Make decision based on both checks
    approved = (
        credit_result.get("score", 0) > 650 and
        fraud_result.get("risk_level") == "low"
    )
    
    return {
        "approved": approved,
        "credit_score": credit_result.get("score"),
        "fraud_risk": fraud_result.get("risk_level"),
    }
