import {
  DurableContext,
  Serdes,
  SerdesContext,
  StepConfig,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

interface Order {
  id: string;
  total: string;
}

const orderSerdes: Serdes<Order> = {
  serialize: async (value: Order | undefined, _ctx: SerdesContext) =>
    value !== undefined ? JSON.stringify(value) : undefined,
  deserialize: async (data: string | undefined, _ctx: SerdesContext) =>
    data !== undefined ? (JSON.parse(data) as Order) : undefined,
};

const stepConfig: StepConfig<Order> = { serdes: orderSerdes };

export const handler = withDurableExecution(
  async (event: unknown, context: DurableContext) => {
    const order = await context.step(
      "fetch-order",
      async (): Promise<Order> => ({ id: "order-123", total: "99.99" }),
      stepConfig,
    );
    return order;
  },
);
