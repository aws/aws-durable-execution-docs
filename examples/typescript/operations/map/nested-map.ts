import {
  BatchResult,
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

type Region = { name: string; items: string[] };

export const handler = withDurableExecution(
  async (event: { regions: Region[] }, context: DurableContext): Promise<string[][]> => {
    const result: BatchResult<string[]> = await context.map(
      "process-regions",
      event.regions,
      async (ctx, region, index) => {
        const inner: BatchResult<string> = await ctx.map(
          `process-${region.name}`,
          region.items,
          async (innerCtx, item, i) =>
            innerCtx.step(`item-${i}`, async () => item.toUpperCase()),
        );
        return inner.getResults();
      },
    );

    return result.getResults();
  },
);
