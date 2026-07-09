import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";
import { examplePlugin } from "./example-plugin";

export const handler = withDurableExecution(
  async (event, context: DurableContext) => {
    return await context.step("process", async () => "done");
  },
  { plugins: [examplePlugin] },
);
