import {
  DurableContext,
  StepConfig,
  StepSemantics,
  createRetryStrategy,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

async function processData(data: string): Promise<object> {
  return { processed: data, status: "completed" };
}

const stepConfig: StepConfig<object> = {
  retryStrategy: createRetryStrategy({
    maxAttempts: 3,
  }),
  semantics: StepSemantics.AtLeastOncePerRetry,
};

export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    const result = await context.step(
      "process_data",
      async () => processData(event.data),
      stepConfig,
    );
    return result;
  },
);
