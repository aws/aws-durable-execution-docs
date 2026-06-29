import { withDurableExecution, DurableContext } from '@aws/durable-execution-sdk-js';

export const handler = withDurableExecution(async (event: any, context: DurableContext) => {
  const compensations: Array<{ name: string; fn: () => Promise<void> }> = [];

  try {
    // Reserve inventory AND verify address in parallel
    const preChecks = await context.parallel('pre-checks', [
      async (ctx) => ctx.step('reserve-inventory', async () =>
        reserveInventory(event.order_id, event.items)
      ),
      async (ctx) => ctx.step('verify-address', async () =>
        verifyAddress(event.address)
      ),
    ]);

    const [reservation, isValidAddress] = preChecks.getResults() as [{ id: string }, { valid: boolean }];

    // Only reserve-inventory needs a compensation because verify-address is stateless
    compensations.push({
      name: 'cancel-reservation',
      fn: () => cancelReservation(reservation.id)
    });

    // Stop execution if address is invalid, catch block will cancel reservation
    if (!isValidAddress.valid) {
      throw new Error('Invalid shipping address');
    }

    const payment = await context.step('charge-payment', async () =>
      chargePayment(event.payment_method, event.amount)
    );
    compensations.push({
      name: 'refund-payment',
      fn: () => refundPayment(payment.id)
    });

    const shipment = await context.step('create-shipment', async () =>
      createShipment(event.order_id, event.address)
    );

    return { success: true, tracking_id: shipment.tracking_id };

  } catch (error) {
    context.logger.error('Order failed, running compensations', { error });

    for (const comp of compensations.reverse()) {
      try {
        await context.step(comp.name, async () => comp.fn());
      } catch (compError) {
        context.logger.error(`Compensation failed: ${comp.name}`, compError);
      }
    }

    throw error;
  }
});

// Mock APIs for demonstration purposes

async function reserveInventory(orderId: string, items: any[]): Promise<{ id: string }> {
  return { id: `RES-${orderId}` };
}

async function cancelReservation(reservationId: string): Promise<void> {
  console.log(`Reservation ${reservationId} cancelled`);
}

async function verifyAddress(address: any): Promise<{ valid: boolean }> {
  return { valid: true };
}

async function chargePayment(method: any, amount: number): Promise<{ id: string }> {
  return { id: `PAY-${amount}` };
}

async function refundPayment(paymentId: string): Promise<void> {
  console.log(`Payment ${paymentId} refunded`);
}

async function createShipment(orderId: string, address: any): Promise<{ tracking_id: string }> {
  return { tracking_id: `TRACK-${orderId}` };
}
