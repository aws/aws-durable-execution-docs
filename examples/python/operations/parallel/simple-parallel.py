from aws_durable_execution_sdk_python import (
    BatchResult,
    DurableContext,
    durable_execution,
)


def check_inventory(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "inventory ok", name="check-inventory")


def check_payment(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "payment ok", name="check-payment")


def check_shipping(ctx: DurableContext) -> str:
    return ctx.step(lambda _: "shipping ok", name="check-shipping")


@durable_execution
def handler(event: dict, context: DurableContext) -> list[str]:
    result: BatchResult[str] = context.parallel(
        [check_inventory, check_payment, check_shipping],
        name="check-services",
    )
    return result.to_dict()
