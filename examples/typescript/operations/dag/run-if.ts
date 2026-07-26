import {
  DurableContext,
  TaskStatus,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

type RefundOutcome = {
  refund: TaskStatus | undefined;
  notify: TaskStatus | undefined;
};

export const handler = withDurableExecution(
  async (_event: unknown, context: DurableContext): Promise<RefundOutcome> => {
    const result = await context.dag("conditional-refund", (dag) => {
      const order = dag.step("load-order", [], async () => ({ total: 0 }));

      const refund = dag.step(
        "issue-refund",
        [order],
        async (deps) => `refunded ${deps["load-order"].total}`,
        { runIf: (deps) => deps["load-order"].total > 0 },
      );

      dag.step("notify", [refund], async (deps) => `sent: ${deps["issue-refund"]}`);
    });

    return {
      refund: result.getStatus("issue-refund"),
      notify: result.getStatus("notify"),
    };
  },
);
