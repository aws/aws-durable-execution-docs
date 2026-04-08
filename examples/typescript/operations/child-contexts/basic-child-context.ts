import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(async (event: any, context: DurableContext) => {
  const result = await context.runInChildContext("process-order", async (child) => {
    const validated = await child.step("validate", async () => validate(event.orderId));
    const charged = await child.step("charge", async () => charge(validated));
    return charged;
  });
  return result;
});

async function validate(orderId: string) { return { orderId, valid: true }; }
async function charge(order: { orderId: string; valid: boolean }) { return { ...order, charged: true }; }
