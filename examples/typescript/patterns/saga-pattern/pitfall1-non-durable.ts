// WRONG - not durable, not retried on failure
for (const comp of compensations.reverse()) {
  await comp.fn(); // if this throws error, subsequent compensations don't run
}

// CORRECT - each compensation is a durable step
for (const comp of compensations.reverse()) {
  try {
    await context.step(comp.name, async () => comp.fn());
  } catch (compError) {
    context.logger.error(`Compensation failed: ${comp.name}`, compError);
  }
}