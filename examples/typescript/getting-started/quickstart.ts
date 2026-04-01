import { withDurableExecution } from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(async (event, context) => {
  const message = await context.step("step-1", (stepCtx) => {
    stepCtx.logger.info("Hello from step-1");
    return "Hello from Durable Lambda!";
  });

  // Pause for 10 seconds without consuming CPU or incurring usage charges
  await context.wait({ seconds: 10 });

  // Replay-aware: logs once even though the function replays after the wait
  context.logger.info("Resumed after wait");

  return { statusCode: 200, body: message };
});
