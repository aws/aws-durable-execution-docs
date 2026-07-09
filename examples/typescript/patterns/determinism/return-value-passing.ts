// Wrong: the step body pushes to an outer list. Replay returns the
// cached undefined without running the body, so the list stays empty
// and the handler returns { receipts: [] } on replay.
export const handler = withDurableExecution(async (event, context) => {
  const receipts: string[] = [];
  for (const item of event.items) {
    await context.step(`save-${item.id}`, async () => {
      const receipt = await saveItem(item);
      receipts.push(receipt.id);
    });
  }
  return { receipts };
});

// Right: the step returns the receipt id. The handler appends the
// returned value to the outer list, which replay rebuilds from the
// cached step results.
export const handler = withDurableExecution(async (event, context) => {
  const receipts: string[] = [];
  for (const item of event.items) {
    const receiptId = await context.step(`save-${item.id}`, async () => {
      const receipt = await saveItem(item);
      return receipt.id;
    });
    receipts.push(receiptId);
  }
  return { receipts };
});
