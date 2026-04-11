import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(async (event, context: DurableContext) => {
  context.logger.info("Starting workflow");

  const result = await context.step("process", async () => {
    return "done";
  });

  context.logger.info("Workflow complete", { result });
  return result;
});
