import {
  withDurableExecution,
  createLinearRetryStrategy,
  StepConfig,
} from "@aws/durable-execution-sdk-js";

const retryStrategy = createLinearRetryStrategy({
  maxAttempts: 5,
  initialDelay: { seconds: 2 },
  increment: { seconds: 3 },
  maxDelay: { seconds: 30 },
});

const stepConfig: StepConfig<string> = { retryStrategy };

export const handler = withDurableExecution(async (event, context) => {
  return context.step(
    "call-external-api",
    async () => callExternalApi(),
    stepConfig,
  );
});

async function callExternalApi(): Promise<string> {
  return "ok";
}
