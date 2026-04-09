import {
  BatchResult,
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    const result: BatchResult<string> = await context.parallel(
      "tasks",
      [
        async (ctx) => ctx.step("task-1", async () => "ok"),
        async (ctx) =>
          ctx.step("task-2", async () => {
            throw new Error("task 2 failed");
          }),
        async (ctx) => ctx.step("task-3", async () => "ok"),
      ],
      { completionConfig: { toleratedFailureCount: 1 } },
    );

    return {
      succeeded: result.successCount,
      failed: result.failureCount,
      results: result.getResults(),
      errors: result.getErrors().map((e) => e.message),
    };
  },
);
