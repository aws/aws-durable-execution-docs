// Stable, descriptive name.
await context.step("validate-order", async () => validateOrder(order));

// Dynamic but deterministic: include the item ID from the input.
await context.step(`save-item-${item.id}`, async () => saveItem(item));

// Wrong: non-deterministic name, changes on replay.
await context.step(`run-${Date.now()}`, async () => doThing());
