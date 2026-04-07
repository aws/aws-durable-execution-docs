import {
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    const [callbackPromise, callbackId] = await context.createCallback(
      "wait-for-approval",
    );

    // Send callbackId to the external system that will resume this function.
    await sendApprovalRequest(callbackId, event.requestId);

    // Execution suspends here until the external system calls back.
    const result = await callbackPromise;
    return { approved: true, result };
  },
);
