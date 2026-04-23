// Business logic: no DurableContext, pure work.
async function validateOrder(order: Order): Promise<ValidationResult> {
  return orderValidator.validate(order);
}

async function chargePayment(order: Order): Promise<Receipt> {
  return paymentService.charge(order.total, order.cardToken);
}

async function scheduleShipment(order: Order): Promise<ShipmentId> {
  return shipmentService.schedule(order.id, order.address);
}

// Orchestration: the handler reads as a sequence of intent.
export const handler = withDurableExecution(
  async (order: Order, context: DurableContext) => {
    await context.step("validate", () => validateOrder(order));
    const receipt = await context.step("charge", () => chargePayment(order));
    const shipmentId = await context.step("schedule", () => scheduleShipment(order));
    return { receipt, shipmentId };
  },
);
