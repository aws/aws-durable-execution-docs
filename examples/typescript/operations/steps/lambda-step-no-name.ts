import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    // Lambda without a name — no automatic name
    const result = await context.step(async () => "some value");
    return result;
  },
);
