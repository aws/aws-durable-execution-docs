import {
  BatchResult,
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: any, context: DurableContext): Promise<string | undefined> => {
    const result: BatchResult<string> = await context.parallel(
      "fetch-data",
      [
        async (ctx) => ctx.step("primary", async () => "primary result"),
        async (ctx) => ctx.step("secondary", async () => "secondary result"),
        async (ctx) => ctx.step("cache", async () => "cache result"),
      ],
      {
        maxConcurrency: 2,
        completionConfig: { minSuccessful: 1 },
      },
    );

    return result.getResults()[0];
  },
);
