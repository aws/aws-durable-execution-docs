import { Serdes, SerdesContext } from "@aws/durable-execution-sdk-js";

interface Order {
  id: string;
  total: string;
}

const orderSerdes: Serdes<Order> = {
  serialize: async (value: Order | undefined, _ctx: SerdesContext) => {
    if (value === undefined) return undefined;
    return JSON.stringify(value);
  },
  deserialize: async (data: string | undefined, _ctx: SerdesContext) => {
    if (data === undefined) return undefined;
    return JSON.parse(data) as Order;
  },
};
