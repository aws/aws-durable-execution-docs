import { withDurableExecution, StepError } from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(async (event, context) => {
  try {
    const result = await context.step("process-order", async (stepCtx) => {
      const orderId = (event as { orderId?: string }).orderId;
      if (!orderId) {
        throw new Error("orderId is required");
      }
      return { orderId, status: "processed" };
    });
    return result;
  } catch (err) {
    if (err instanceof StepError) {
      context.logger.error("Step failed", { cause: err.cause?.message });
      return { error: err.cause?.message ?? "Step failed" };
    }
    throw err;
  }
});
