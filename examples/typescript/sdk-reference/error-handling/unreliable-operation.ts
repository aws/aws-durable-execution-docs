import {
  DurableContext,
  StepConfig,
  createRetryStrategy,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

class TransientError extends Error {}

const stepConfig: StepConfig<string> = {
  retryStrategy: createRetryStrategy({
    maxAttempts: 3,
    retryableErrorTypes: [TransientError],
  }),
};

async function unreliableOperation(): Promise<string> {
  if (Math.random() > 0.5) throw new TransientError("Random error occurred");
  return "Operation succeeded";
}

export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    const result = await context.step(
      "unreliable_operation",
      async () => unreliableOperation(),
      stepConfig,
    );
    return result;
  },
);
