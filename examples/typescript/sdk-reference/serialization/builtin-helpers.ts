import {
  DurableContext,
  createClassSerdes,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

class Order {
  id: string = "";
  total: string = "";

  label() {
    return `Order ${this.id} — ${this.total}`;
  }
}

const orderSerdes = createClassSerdes(Order);

export const handler = withDurableExecution(
  async (event: unknown, context: DurableContext) => {
    const order = await context.step(
      "fetch-order",
      async () => Object.assign(new Order(), { id: "order-123", total: "99.99" }),
      { serdes: orderSerdes },
    );
    // order.label() works — class methods are preserved after deserialization
    return order.label();
  },
);
