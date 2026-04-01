import {
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: { data: string }, context: DurableContext) => {
    const result = await context.step("my-step", async () => {
      // Your business logic here
      return `processed-${event.data}`;
    });
    return result;
  },
);
