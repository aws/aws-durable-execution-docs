// WRONG - timestamp generated outside a step, changes on replay
const timestamp = Date.now();
compensations.push({
  name: 'cancel',
  fn: () => cancelWithTimestamp(reservation.id, timestamp) // different on replay!
});

// CORRECT - data comes from step return values (checkpointed, stable on replay)
const reservation = await context.step('reserve', async () => reserveInventory());
compensations.push({
  name: 'cancel',
  fn: () => cancelReservation(reservation.id) // reservation.id from checkpoint, stable
});