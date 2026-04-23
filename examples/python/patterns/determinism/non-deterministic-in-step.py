import uuid
from aws_durable_execution_sdk_python import DurableContext, durable_execution, durable_step
from aws_durable_execution_sdk_python.types import StepContext


@durable_step
def generate_transaction_id(ctx: StepContext) -> str:
    return str(uuid.uuid4())


@durable_step
def charge(ctx: StepContext, amount: float, transaction_id: str) -> dict:
    return payment_service.charge(amount, transaction_id)


@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    transaction_id = context.step(generate_transaction_id())
    receipt = context.step(charge(event["amount"], transaction_id))
    return {"transactionId": transaction_id, "receipt": receipt}
