import {
  BatchResult,
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: any, context: DurableContext): Promise<string[][]> => {
    const outer: BatchResult<string[]> = await context.parallel("outer", [
      async (ctx) => {
        const inner: BatchResult<string> = await ctx.parallel("inner-a", [
          async (c) => c.step("a1", async () => "a1"),
          async (c) => c.step("a2", async () => "a2"),
        ]);
        return inner.getResults();
      },
      async (ctx) => {
        const inner: BatchResult<string> = await ctx.parallel("inner-b", [
          async (c) => c.step("b1", async () => "b1"),
          async (c) => c.step("b2", async () => "b2"),
        ]);
        return inner.getResults();
      },
    ]);

    return outer.getResults();
  },
);
