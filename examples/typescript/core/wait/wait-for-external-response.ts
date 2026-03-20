export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    // Wait for external approval
    const result = await context.waitForCallback(
      "approval_wait",
      async (callbackId) => {
        // Send callback_id to external approval system
        await sendToApprovalSystem(callbackId);
      },
    );
    return result;
  },
);
