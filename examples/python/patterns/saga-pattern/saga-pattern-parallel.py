from aws_durable_execution_sdk_python import (
    BatchResult,
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
def verify_address(ctx: StepContext, address: dict) -> dict:
    # Stateless — no compensation needed
    return {"valid": True}


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
        order_id = event["order_id"]
        amount   = event["amount"]
        address  = event["address"]

        # Reserve inventory AND verify address in parallel
        pre_checks: BatchResult = context.parallel(
            [
                lambda ctx: ctx.step(reserve_inventory(order_id)),
                lambda ctx: ctx.step(verify_address(address)),
            ],
            name="pre-checks",
        )

        reservation, address_verification = pre_checks.get_results()

        compensations.append(("cancel-reservation", cancel_reservation, reservation["id"]))

        # Stop execution if address is invalid
        if not address_verification.get("valid"):
            raise ValueError("Invalid shipping address")

        payment = context.step(charge_payment(amount))
        compensations.append(("refund-payment", refund_payment, payment["id"]))

        shipment = context.step(create_shipment(order_id))

        return {"success": True, "tracking_id": shipment["tracking_id"]}

    except Exception as error:
        context.logger.error("Order failed, running compensations", error)

        for name, comp_step, resource_id in reversed(compensations):
            try:
                context.step(comp_step(resource_id), name=name)
            except Exception as comp_error:
                context.logger.error(f"Compensation failed: {name}", comp_error)

        raise error
