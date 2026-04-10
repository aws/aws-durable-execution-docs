import {
  withDurableExecution,
  createRetryStrategy,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(async (event, context) => {
  // The SDK throws a TypeError for invalid configuration values.
  // For example, passing a negative delay to createRetryStrategy:
  try {
    const retryStrategy = createRetryStrategy({
      initialDelay: { seconds: -1 }, // invalid: negative delay
      maxAttempts: 3,
    });
  } catch (err) {
    if (err instanceof TypeError) {
      context.logger.error("Invalid SDK configuration", { message: (err as Error).message });
      return { error: "InvalidConfiguration" };
    }
    throw err;
  }
  return { status: "ok" };
});
