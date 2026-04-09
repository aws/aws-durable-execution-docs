from aws_durable_execution_sdk_python import DurableContext


def process_order(
    ctx: DurableContext,
    order: dict,
    index: int,
    orders: list[dict],
) -> dict:
    def validate(_):
        if order["amount"] <= 0:
            raise ValueError("Invalid amount")
        return order

    validated = ctx.step(validate, name="validate")
    charged = ctx.step(lambda _: validated["amount"], name="charge")
    return {"orderId": validated["id"], "charged": charged}
