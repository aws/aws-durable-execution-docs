try {
  const reservation = await context.step('reserve-inventory', async () =>
    reserveInventory(event.orderId)
  );
  compensations.push({
    name: 'cancel-reservation',
    // reservation.id is the idempotency key and service uses it to deduplicate
    fn: () => cancelReservation(reservation.id)
  });

  // ... more steps ...

} catch (error) {
  for (const comp of compensations.reverse()) {
    try {
      await context.step(comp.name, async () => comp.fn());
    } catch (compError) {
      context.logger.error(`Compensation failed: ${comp.name}`, compError);
    }
  }
  throw error;
}