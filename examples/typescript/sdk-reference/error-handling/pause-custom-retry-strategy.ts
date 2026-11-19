import {
  withDurableExecution,
  RetryDecision,
} from "@aws/durable-execution-sdk-js";

class DownstreamUnavailableError extends Error {}

const retryStrategy = (error: Error, attemptCount: number): RetryDecision => {
  if (attemptCount >= 10) {
    return { shouldRetry: false };
  }
  return {
    shouldRetry: true,
    delay: { seconds: 2 },
    pause: attemptCount === 5 && error instanceof DownstreamUnavailableError,
  };
};

export const handler = withDurableExecution(async (event, context) => {
  const result = await context.step(
    "call-downstream",
    async () => callDownstream(),
    { retryStrategy },
  );
  return result;
});

async function callDownstream(): Promise<string> {
  return "ok";
}
