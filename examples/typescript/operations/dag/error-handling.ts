import {
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

type Summary = {
  failures: { name: string; error: string | undefined }[];
  shipStatus: string | undefined;
  metricsStatus: string | undefined;
};

export const handler = withDurableExecution(
  async (_event: unknown, context: DurableContext): Promise<Summary> => {
    const result = await context.dag("charge-and-ship", (dag) => {
      const charge = dag.step("charge-card", [], async () => {
        throw new Error("card declined");
      });

      dag.step("ship-order", [charge], async () => "shipped");
      dag.step("record-metrics", [], async () => "recorded");
    });

    const failures = result.failed().map((task) => ({
      name: task.name,
      error: task.error?.message,
    }));

    return {
      failures,
      shipStatus: result.getStatus("ship-order"),
      metricsStatus: result.getStatus("record-metrics"),
    };
  },
);
