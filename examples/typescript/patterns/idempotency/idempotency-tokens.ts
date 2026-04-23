const idempotencyKey = await context.step("idempotency-key", async () =>
  randomUUID(),
);

const charge = await context.step("charge", async () => {
  return paymentService.charge({
    amount: event.amount,
    cardToken: event.cardToken,
    idempotencyKey,
  });
});
