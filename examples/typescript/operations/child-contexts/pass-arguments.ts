import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(async (event: any, context: DurableContext) => {
  const { orderId, userId } = event;

  // Capture arguments in a closure
  const result = await context.runInChildContext("process-order", async (child) => {
    const validated = await child.step("validate", async () => validate(orderId, userId));
    return await child.step("charge", async () => charge(validated));
  });

  return result;
});

async function validate(orderId: string, userId: string) { return { orderId, userId }; }
async function charge(order: { orderId: string; userId: string }) { return { ...order, charged: true }; }
