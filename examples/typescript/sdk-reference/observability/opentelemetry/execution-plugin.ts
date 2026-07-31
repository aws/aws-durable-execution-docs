import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";
import { ExecutionOtelPlugin } from "@aws/durable-execution-sdk-js-otel";

export const handler = withDurableExecution(
  async (event: { id: string }, context: DurableContext) => {
    const data = await context.step("fetch-data", async () => event.id);
    await context.wait("cooldown", { seconds: 5 });
    return await context.step("process", async () => `processed ${data}`);
  },
  { plugins: [new ExecutionOtelPlugin()] },
);
