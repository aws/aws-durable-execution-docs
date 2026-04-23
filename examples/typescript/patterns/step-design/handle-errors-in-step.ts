import { createRetryStrategy } from "@aws/durable-execution-sdk-js";

class TransientApiError extends Error {}
class RateLimitError extends Error {}

// Retry only transient errors. Anything else (bad input, not-found) fails immediately.
const retryStrategy = createRetryStrategy({
  maxAttempts: 5,
  initialDelay: { seconds: 2 },
  retryableErrorTypes: [TransientApiError, RateLimitError],
});

await context.step(
  "call-api",
  async () => externalApi.get(event.id),
  { retryStrategy },
);
