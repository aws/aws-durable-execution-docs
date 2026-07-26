import {
  DurableContext,
  TaskHandle,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (_event: unknown, context: DurableContext): Promise<number> => {
    let double!: TaskHandle<"double", number>;

    const result = await context.dag("compute", (dag) => {
      double = dag.step("double", [], async () => 42);
    });

    return result.getResult(double)!;
  },
);
