import {
  withDurableExecution,
  withRetry,
  createRetryStrategy,
} from "@aws/durable-execution-sdk-js";

const retryStrategy = createRetryStrategy({
  maxAttempts: 3,
  initialDelay: { seconds: 2 },
  backoffRate: 2,
});

export const handler = withDurableExecution(async (event, context) => {
  // invoke does not accept a retryStrategy, so wrap it with withRetry to
  // apply backoff between failed attempts.
  const receipt = await withRetry(
    context,
    "charge-payment",
    (ctx, attempt) =>
      ctx.invoke<{ orderId: string }, string>(
        `charge-${attempt}`,
        "process-payment",
        { orderId: (event as { orderId: string }).orderId },
      ),
    { retryStrategy },
  );
  return receipt;
});
