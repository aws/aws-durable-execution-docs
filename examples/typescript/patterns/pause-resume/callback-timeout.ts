const outcome = await context.waitForCallback(
  "wait-for-approval",
  async (callbackId) => {
    await approvalsService.request({ id: event.orderId, callbackId });
  },
  { timeout: { hours: 24 } },
);
