import {
  withDurableExecution,
  createRetryStrategy,
  StepConfig,
} from "@aws/durable-execution-sdk-js";

const retryStrategy = createRetryStrategy({
  maxAttempts: 5,
  initialDelay: { seconds: 2 },
  maxDelay: { minutes: 1 },
  backoffRate: 2,
});

const stepConfig: StepConfig<string> = { retryStrategy };

export const handler = withDurableExecution(async (event, context) => {
  const result = await context.step(
    "call-external-api",
    async () => callExternalApi(),
    stepConfig,
  );
  return result;
});

async function callExternalApi(): Promise<string> {
  return "ok";
}
