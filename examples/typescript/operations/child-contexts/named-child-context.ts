import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(async (event: any, context: DurableContext) => {
  // Pass undefined to omit the name
  const unnamed = await context.runInChildContext(async (child) => {
    return await child.step(async () => "result");
  });

  // Pass a name as the first argument
  const named = await context.runInChildContext("process-order", async (child) => {
    return await child.step(async () => "result");
  });

  return { unnamed, named };
});
