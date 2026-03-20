import {
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    // Wait with explicit name
    await context.wait("custom_wait", { seconds: 2 });
    return "Wait with name completed";
  },
);
