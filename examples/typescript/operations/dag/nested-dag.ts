import {
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (_event: unknown, context: DurableContext): Promise<number> => {
    const result = await context.dag(
      "outer",
      (dag) => {
        dag.step("prepare", [], async () => "ready");

        dag.dag(
          "inner",
          [],
          (subDag) => {
            subDag.step("a", [], async () => 1);
            subDag.step("b", [], async () => 2);
          },
          { maxConcurrency: 5 },
        );
      },
      { maxConcurrency: 10 },
    );

    return result.successCount;
  },
);
