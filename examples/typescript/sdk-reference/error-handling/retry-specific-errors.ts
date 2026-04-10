import {
  withDurableExecution,
  createRetryStrategy,
} from "@aws/durable-execution-sdk-js";

class RateLimitError extends Error {}
class ServiceUnavailableError extends Error {}

const retryStrategy = createRetryStrategy({
  maxAttempts: 5,
  initialDelay: { seconds: 2 },
  // Only retry these specific error types; all other errors fail immediately
  retryableErrorTypes: [RateLimitError, ServiceUnavailableError],
});

export const handler = withDurableExecution(async (event, context) => {
  const result = await context.step(
    "call-api",
    async () => {
      // Throws RateLimitError or ServiceUnavailableError on transient failures
      return callApi();
    },
    { retryStrategy },
  );
  return result;
});

async function callApi(): Promise<string> {
  return "ok";
}
