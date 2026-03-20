import {
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    // Wait for 5 seconds
    await context.wait({ seconds: 5 });
    return "Wait completed";
  },
);
