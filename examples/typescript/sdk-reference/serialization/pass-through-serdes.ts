import {
  DurableContext,
  Serdes,
  SerdesContext,
  StepConfig,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

// A pass-through serdes returns the value as-is (already a JSON string).
const passThroughSerdes: Serdes<string> = {
  serialize: async (value: string | undefined, _ctx: SerdesContext) => value,
  deserialize: async (data: string | undefined, _ctx: SerdesContext) => data,
};

export const handler = withDurableExecution(
  async (event: unknown, context: DurableContext) => {
    const config: StepConfig<string> = { serdes: passThroughSerdes };
    const raw = await context.step(
      "fetch-raw",
      async (): Promise<string> => '{"id":"order-123"}',
      config,
    );
    return raw;
  },
);
