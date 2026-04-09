import {
  BatchResult,
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: any, context: DurableContext): Promise<string | undefined> => {
    // Complete as soon as one branch succeeds
    const result: BatchResult<string> = await context.parallel(
      "race",
      [
        async (ctx) => ctx.step("source-a", async () => "result from a"),
        async (ctx) => ctx.step("source-b", async () => "result from b"),
        async (ctx) => ctx.step("source-c", async () => "result from c"),
      ],
      { completionConfig: { minSuccessful: 1 } },
    );

    return result.getResults()[0];
  },
);
