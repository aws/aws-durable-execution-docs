import {
  DurableContext,
  InvokeError,
  durableExecution,
} from "aws-durable-execution-sdk-js";

export const handler = durableExecution(
  async (event: { orderId: string }, context: DurableContext) => {
    try {
      const result = await context.invoke(
        "process-payment",
        "payment-processor-function:live",
        { orderId: event.orderId },
      );
      return { status: "success", result };
    } catch (err) {
      if (err instanceof InvokeError) {
        return { status: "failed", reason: err.message };
      }
      throw err;
    }
  },
);
