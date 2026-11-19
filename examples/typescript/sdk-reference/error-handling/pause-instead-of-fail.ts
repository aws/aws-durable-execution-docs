import {
  withDurableExecution,
  RetryDecision,
} from "@aws/durable-execution-sdk-js";

const PAUSE_FROM_ATTEMPT = 3;

const retryStrategy = (error: Error, attemptCount: number): RetryDecision => ({
  shouldRetry: true,
  delay: { seconds: 5 },
  pause: attemptCount >= PAUSE_FROM_ATTEMPT,
});

export const handler = withDurableExecution(async (event, context) => {
  const result = await context.step(
    "charge-payment",
    async () => chargePayment(),
    { retryStrategy },
  );
  return result;
});

async function chargePayment(): Promise<string> {
  return "ok";
}
