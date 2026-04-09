import {
  BatchResult,
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: any, context: DurableContext): Promise<number[]> => {
    const result: BatchResult<number> = await context.map(
      "square-numbers",
      [1, 2, 3, 4, 5],
      async (ctx, item, index) =>
        ctx.step(`square-${index}`, async () => item * item),
    );

    return result.getResults();
  },
);
