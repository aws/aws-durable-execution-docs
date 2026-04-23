// Wrong: total mutates outside the step, replay restarts it at 0.
export const handler = withDurableExecution(async (event, context) => {
  let total = 0;
  for (const item of event.items) {
    await context.step(`save-${item.id}`, async () => saveItem(item));
    total += item.price;
  }
  return { total };
});

// Right: each step returns the new running total.
export const handler = withDurableExecution(async (event, context) => {
  let total = 0;
  for (const item of event.items) {
    total = await context.step(`save-${item.id}`, async () => {
      await saveItem(item);
      return total + item.price;
    });
  }
  return { total };
});
