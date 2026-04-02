import {
  DurableContext,
  StepContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

async function validateOrder(ctx: StepContext, orderId: string): Promise<object> {
  return { order_id: orderId, valid: true };
}

export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    const orderId = event.order_id;
    const validation = await context.step("validate_order", (ctx) =>
      validateOrder(ctx, orderId),
    );
    return validation;
  },
);
