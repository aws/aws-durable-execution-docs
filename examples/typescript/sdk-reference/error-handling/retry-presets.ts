import { withDurableExecution, retryPresets } from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(async (event, context) => {
  // Default: 6 attempts, 5s initial delay, 60s max, 2x backoff, full jitter
  const result = await context.step(
    "call-api",
    async () => callApi(),
    { retryStrategy: retryPresets.default },
  );

  // Linear: 6 attempts, delays of 1s, 2s, 3s, 4s, 5s
  const audit = await context.step(
    "audit-log",
    async () => writeAuditLog(),
    { retryStrategy: retryPresets.linear },
  );

  // No retry: fail immediately on first error
  const critical = await context.step(
    "charge-payment",
    async () => chargePayment(),
    { retryStrategy: retryPresets.noRetry },
  );

  return { result, audit, critical };
});

async function callApi(): Promise<string> { return "ok"; }
async function writeAuditLog(): Promise<string> { return "logged"; }
async function chargePayment(): Promise<string> { return "charged"; }
