import {
  BatchResult,
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: { items: string[] }, context: DurableContext): Promise<void> => {
    const result: BatchResult<string> = await context.map(
      "process-items",
      event.items,
      async (ctx, item, index) =>
        ctx.step(`process-${index}`, async () => {
          if (item === "bad") throw new Error("bad item");
          return item.toUpperCase();
        }),
    );

    if (result.hasFailure) {
      const errors = result.getErrors();
      console.log(`${result.failureCount} items failed:`, errors);
    }

    const successes = result.getResults();
    console.log(`${result.successCount} items succeeded:`, successes);
  },
);
