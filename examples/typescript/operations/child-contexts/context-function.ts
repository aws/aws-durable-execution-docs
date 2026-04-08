import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(async (event: any, context: DurableContext) => {
  // Pass an inline async function directly
  const result = await context.runInChildContext("process-order", async (child) => {
    const step1 = await child.step("validate", async () => validate(event.orderId));
    const step2 = await child.step("charge", async () => charge(step1));
    return step2;
  });
  return result;
});

async function validate(orderId: string) { return { orderId, valid: true }; }
async function charge(order: { orderId: string; valid: boolean }) { return { ...order, charged: true }; }
