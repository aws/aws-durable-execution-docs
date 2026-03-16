from aws_durable_execution_sdk_python.config import StepConfig, StepSemantics

@durable_step
def charge_credit_card(step_context: StepContext, amount: float) -> dict:
    return {"transaction_id": "txn_123", "status": "completed"}

@durable_execution
def lambda_handler(event: dict, context: DurableContext) -> dict:
    # Prevent duplicate charges on retry
    payment = context.step(
        charge_credit_card(event["amount"]),
        config=StepConfig(step_semantics=StepSemantics.AT_MOST_ONCE_PER_RETRY),
    )
    return payment
