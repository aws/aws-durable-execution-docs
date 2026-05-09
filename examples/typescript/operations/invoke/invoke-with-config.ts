import {
  DurableContext,
  InvokeConfig,
  withDurableExecution,
} from "@aws-durable-execution-sdk-js";

export const handler = withDurableExecution(
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
