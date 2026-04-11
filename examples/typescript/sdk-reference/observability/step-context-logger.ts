import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(async (event, context: DurableContext) => {
  await context.step("process", async (stepCtx) => {
    // stepCtx.logger includes operationId and attempt in every log entry.
    stepCtx.logger.info("Running step");
    return "done";
  });
});
