import uuid

@durable_step
def generate_key(ctx: StepContext) -> str:
    return str(uuid.uuid4())


@durable_step
def charge_with_key(ctx: StepContext, amount: float, card_token: str, key: str) -> dict:
    return payment_service.charge(amount=amount, card_token=card_token, idempotency_key=key)


key = context.step(generate_key(), name="idempotency-key")
receipt = context.step(
    charge_with_key(event["amount"], event["card_token"], key),
    name="charge",
)
