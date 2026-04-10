import {
  withDurableExecution,
  StepConfig,
  RetryDecision,
} from "@aws/durable-execution-sdk-js";

// A retry strategy is a plain function: (error: Error, attemptCount: number) => RetryDecision
// attemptCount is 1-based: 1 on the first retry, 2 on the second, etc.
const customRetryStrategy = (error: Error, attemptCount: number): RetryDecision => {
  if (attemptCount >= 4) {
    return { shouldRetry: false };
  }
  // Fixed 2-second delay regardless of attempt number
  return { shouldRetry: true, delay: { seconds: 2 } };
};

const stepConfig: StepConfig<string> = { retryStrategy: customRetryStrategy };

export const handler = withDurableExecution(async (event, context) => {
  const result = await context.step(
    "call-api",
    async () => callApi(),
    stepConfig,
  );
  return result;
});

async function callApi(): Promise<string> {
  return "ok";
}
