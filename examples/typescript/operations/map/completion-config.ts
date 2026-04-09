import {
  BatchResult,
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: { items: string[] }, context: DurableContext): Promise<string[]> => {
    const result: BatchResult<string> = await context.map(
      "process-items",
      event.items,
      async (ctx, item, index) =>
        ctx.step(`process-${index}`, async () => item.toUpperCase()),
      {
        completionConfig: { minSuccessful: 3 },
      },
    );

    return result.getResults();
  },
);
