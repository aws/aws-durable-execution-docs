from aws_durable_execution_sdk_python.config import StepConfig, StepSemantics

@durable_step
def charge_credit_card(step_context: StepContext, amount: float) -> dict:
    """Charge a credit card - should only happen once."""
    # Payment processing logic
    return {"transaction_id": "txn_123", "status": "completed"}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    # Use at-most-once to prevent duplicate charges
    step_config = StepConfig(
        step_semantics=StepSemantics.AT_MOST_ONCE_PER_RETRY
    )
    
    payment = context.step(
        charge_credit_card(event["amount"]),
        config=step_config,
    )
    
    return payment
