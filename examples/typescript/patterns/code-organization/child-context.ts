await context.runInChildContext("process-order", async (child) => {
  await child.step("validate", () => validate(order));
  const receipt = await child.step("charge", () => charge(order));
  await child.step("schedule", () => schedule(order, receipt));
  return "ok";
});
