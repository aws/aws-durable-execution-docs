import { withDurableExecution, DurableContext } from '@aws/durable-execution-sdk-js';

export const handler = withDurableExecution(async (event: any, context: DurableContext) => {
  
  const compensations: Array<{ name: string; fn: () => Promise<void> }> = [];

  try {
    // Forward steps: each registers a compensation on success
    const reservation = await context.step('reserve-inventory', async () =>
      reserveInventory(event.orderId)
    );
    compensations.push({
      name: 'cancel-reservation',
      fn: () => cancelReservation(reservation.id)
    });

    const payment = await context.step('charge-payment', async () =>
      chargePayment(event.amount)
    );
    compensations.push({
      name: 'refund-payment',
      fn: () => refundPayment(payment.id)
    });

    await context.step('create-shipment', async () =>
      createShipment(event.orderId)
    );

    return { success: true };

  } catch (error) {
    // Run compensations in reverse to undo completed steps
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

async function reserveInventory(orderId: string): Promise<{ id: string }> {
  return { id: `RES-${orderId}` };
}

async function cancelReservation(reservationId: string): Promise<void> {
  console.log(`Reservation ${reservationId} cancelled`);
}

async function chargePayment(amount: number): Promise<{ id: string }> {
  return { id: `PAY-${amount}` };
}

async function refundPayment(paymentId: string): Promise<void> {
  console.log(`Payment ${paymentId} refunded`);
}

async function createShipment(orderId: string): Promise<{ trackingId: string }> {
  return { trackingId: `TRACK-${orderId}` };
}
