import {
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (_event: unknown, context: DurableContext): Promise<number> => {
    const result = await context.dag(
      "sync-accounts",
      (dag) => {
        dag.step("account-a", [], async () => "a");
        dag.step("account-b", [], async () => "b");
        dag.step("account-c", [], async () => "c");
      },
      {
        maxConcurrency: 2,
        completionConfig: { toleratedFailureCount: 1 },
      },
    );

    return result.successCount;
  },
);
