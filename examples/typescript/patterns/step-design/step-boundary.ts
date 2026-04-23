// Wrong: calling context.step inside a step callback is invalid.
await context.step("outer", async (ctx) => {
  await context.step("inner", async () => work()); // ERROR
});

// Right: group durable operations in a child context.
await context.runInChildContext("order-pipeline", async (child) => {
  await child.step("validate", async () => validate());
  await child.step("charge", async () => charge());
  return "done";
});
