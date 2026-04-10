from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_step,
    ExecutionError,
    StepContext,
)

@durable_step
def process_payment(step_context: StepContext, amount: float, card: str) -> dict:
    """Process payment with validation."""
    # Validate card
    if not is_valid_card(card):
        # Don't retry invalid cards
        raise ExecutionError("Invalid card number")
    
    # Process payment
    return {"transaction_id": "txn_123", "amount": amount}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    """Handle payment with error handling."""
    try:
        result = context.step(
            process_payment(event["amount"], event["card"])
        )
        return {"status": "success", "transaction": result}
    except ExecutionError as e:
        # Permanent failure, don't retry
        return {"status": "failed", "error": str(e)}
