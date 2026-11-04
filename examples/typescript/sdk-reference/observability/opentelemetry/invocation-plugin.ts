import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";
import { createOtelPluginFactory } from "@aws/durable-execution-sdk-js-otel";

export const handler = withDurableExecution(
  async (event, context: DurableContext) => {
    return await context.step("process", async () => "done");
  },
  { plugins: [createOtelPluginFactory({ mode: "invocation" })] },
);
