import { withDurableExecution, retryPresets } from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(async (event, context) => {
  // Default: 6 attempts, 5s initial delay, 60s max, 2x backoff, full jitter
  const result = await context.step(
    "call-api",
    async () => callApi(),
    { retryStrategy: retryPresets.default },
  );

  // No retry: fail immediately on first error
  const critical = await context.step(
    "charge-payment",
    async () => chargePayment(),
    { retryStrategy: retryPresets.noRetry },
  );

  return { result, critical };
});

async function callApi(): Promise<string> { return "ok"; }
async function chargePayment(): Promise<string> { return "charged"; }
