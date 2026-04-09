import { DurableContext } from "@aws/durable-execution-sdk-js";

type Order = { id: string; amount: number };
type Receipt = { orderId: string; charged: number };

async function processOrder(
  ctx: DurableContext,
  order: Order,
  index: number,
  orders: Order[],
): Promise<Receipt> {
  const validated = await ctx.step("validate", async () => {
    if (order.amount <= 0) throw new Error("Invalid amount");
    return order;
  });
  const charged = await ctx.step("charge", async () => validated.amount);
  return { orderId: validated.id, charged };
}
