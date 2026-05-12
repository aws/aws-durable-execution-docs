from aws_durable_execution_sdk_python import (
    DurableContext,
    StepContext,
    durable_execution,
    durable_step,
)

@durable_step
def reserve_inventory(ctx: StepContext, order_id: str) -> dict:
    return {"id": f"RES-{order_id}"}


@durable_step
def cancel_reservation(ctx: StepContext, reservation_id: str) -> None:
    print(f"Reservation {reservation_id} cancelled")


@durable_step
def charge_payment(ctx: StepContext, amount: float) -> dict:
    return {"id": f"PAY-{amount}"}


@durable_step
def refund_payment(ctx: StepContext, payment_id: str) -> None:
    print(f"Payment {payment_id} refunded")


@durable_step
def create_shipment(ctx: StepContext, order_id: str) -> dict:
    return {"tracking_id": f"TRACK-{order_id}"}

@durable_execution
def handler(event: dict, context: DurableContext) -> dict:
    compensations = []

    try:
        # Forward steps: each registers a compensation on success
        reservation = context.step(reserve_inventory(event["order_id"]))
        compensations.append(("cancel-reservation", cancel_reservation, reservation["id"]))

        payment = context.step(charge_payment(event["amount"]))
        compensations.append(("refund-payment", refund_payment, payment["id"]))

        context.step(create_shipment(event["order_id"]))

        return {"success": True}

    except Exception as error:
        context.logger.error("Order failed, running compensations", error)

        # Run compensations in reverse to undo completed steps
        for name, comp_step, resource_id in reversed(compensations):
            try:
                context.step(comp_step(resource_id), name=name)
            except Exception as comp_error:
                context.logger.error(f"Compensation failed: {name}", comp_error)

        raise error
