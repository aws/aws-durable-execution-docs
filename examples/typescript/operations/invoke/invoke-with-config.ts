import {
  DurableContext,
  InvokeConfig,
  durableExecution,
} from "aws-durable-execution-sdk-js";

export const handler = durableExecution(
  async (event: { orderId: string }, context: DurableContext) => {
    const config: InvokeConfig<{ orderId: string }, { status: string }> = {
      tenantId: event.orderId,
    };

    const result = await context.invoke(
      "process-order",
      "order-processor-function:live",
      { orderId: event.orderId },
      config,
    );

    return result;
  },
);
