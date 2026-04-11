import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(async (event, context: DurableContext) => {
  // Pass modeAware: false to emit logs on every replay.
  context.configureLogger({ modeAware: false });

  context.logger.info("This logs on every replay");

  await context.step("step-1", async () => {
    return "result";
  });
});
