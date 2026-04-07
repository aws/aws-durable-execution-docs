import {
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    const [callbackPromise, callbackId] = await context.createCallback(
      "wait-for-payment",
      {
        timeout: { hours: 24 },
        heartbeatTimeout: { minutes: 30 },
      },
    );

    await submitPaymentRequest(callbackId, event.amount);
    return await callbackPromise;
  },
);
