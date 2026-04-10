import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

interface Order {
  id: string;
  total: string;
}

export const handler = withDurableExecution(
  async (event: unknown, context: DurableContext) => {
    // No SerDes config — the SDK serializes and deserializes the result automatically.
    const order = await context.step("fetch-order", async (): Promise<Order> => {
      return { id: "order-123", total: "99.99" };
    });
    return order;
  },
);
