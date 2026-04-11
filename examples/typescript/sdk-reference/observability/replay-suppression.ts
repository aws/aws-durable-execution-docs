import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(async (event, context: DurableContext) => {
  // context.logger suppresses duplicate logs during replay by default.
  // Logs from completed operations do not repeat when the SDK replays.
  context.logger.info("Step 1 starting");

  await context.step("step-1", async () => {
    return "result";
  });

  context.logger.info("Step 1 complete");
});
