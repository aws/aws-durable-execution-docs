import {
  BatchResult,
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: { urls: string[] }, context: DurableContext): Promise<string[]> => {
    const result: BatchResult<string> = await context.map(
      "fetch-urls",
      event.urls,
      async (ctx, url, index) =>
        ctx.step(`fetch-${index}`, async () => {
          const response = await fetch(url);
          return response.text();
        }),
      {
        maxConcurrency: 5,
        completionConfig: { toleratedFailureCount: 2 },
      },
    );

    return result.getResults();
  },
);
