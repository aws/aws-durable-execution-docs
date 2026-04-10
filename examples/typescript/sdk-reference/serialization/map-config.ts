import {
  BatchResult,
  DurableContext,
  MapConfig,
  Serdes,
  SerdesContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

interface ProcessedItem {
  id: string;
  status: string;
}

const itemSerdes: Serdes<ProcessedItem> = {
  serialize: async (value: ProcessedItem | undefined, _ctx: SerdesContext) =>
    value !== undefined ? JSON.stringify(value) : undefined,
  deserialize: async (data: string | undefined, _ctx: SerdesContext) =>
    data !== undefined ? (JSON.parse(data) as ProcessedItem) : undefined,
};

const mapConfig: MapConfig<string, ProcessedItem> = {
  itemSerdes,
};

export const handler = withDurableExecution(
  async (event: unknown, context: DurableContext) => {
    const items = ["a", "b", "c"];
    const result: BatchResult<ProcessedItem> = await context.map(
      "process-items",
      items,
      async (_ctx, item) => ({ id: item, status: "done" }),
      mapConfig,
    );
    return result.getResults();
  },
);
