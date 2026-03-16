from aws_durable_execution_sdk_python import (
    DurableContext,
    durable_execution,
    durable_step,
    StepContext,
)

@durable_step
def validate_order(step_context: StepContext, order_id: str) -> dict:
    return {"order_id": order_id, "status": "validated"}

@durable_step
def process_payment(step_context: StepContext, order: dict) -> dict:
    return {**order, "payment_status": "completed"}

@durable_step
def fulfill_order(step_context: StepContext, order: dict) -> dict:
    return {**order, "fulfillment_status": "shipped"}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    order_id = event["order_id"]
    
    validated = context.step(validate_order(order_id), name="validate")
    paid = context.step(process_payment(validated), name="payment")
    fulfilled = context.step(fulfill_order(paid), name="fulfillment")
    
    return fulfilled
