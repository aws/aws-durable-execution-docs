def validate_order(order: dict) -> dict:
    return order_validator.validate(order)


def charge_payment(order: dict) -> dict:
    return payment_service.charge(order["total"], order["card_token"])


def schedule_shipment(order: dict) -> str:
    return shipment_service.schedule(order["id"], order["address"])


@durable_step
def validate_step(ctx: StepContext, order: dict) -> dict:
    return validate_order(order)


@durable_step
def charge_step(ctx: StepContext, order: dict) -> dict:
    return charge_payment(order)


@durable_step
def schedule_step(ctx: StepContext, order: dict) -> str:
    return schedule_shipment(order)


@durable_execution
def handler(order: dict, context: DurableContext) -> dict:
    context.step(validate_step(order))
    receipt = context.step(charge_step(order))
    shipment_id = context.step(schedule_step(order))
    return {"receipt": receipt, "shipmentId": shipment_id}
