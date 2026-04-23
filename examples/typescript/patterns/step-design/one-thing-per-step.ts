// Wrong: one step for three unrelated side effects.
await context.step("process-order", async () => {
  await chargePayment(order);
  await sendConfirmationEmail(order);
  await updateInventory(order);
});

// Right: each side effect gets its own step.
await context.step("charge-payment", async () => chargePayment(order));
await context.step("send-confirmation", async () => sendConfirmationEmail(order));
await context.step("update-inventory", async () => updateInventory(order));
