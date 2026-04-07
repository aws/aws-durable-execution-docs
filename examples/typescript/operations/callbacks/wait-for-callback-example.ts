import {
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    const result = await context.waitForCallback(
      "wait-for-approval",
      async (callbackId) => {
        await sendApprovalRequest(callbackId, event.requestId);
      },
    );
    return { approved: true, result };
  },
);
