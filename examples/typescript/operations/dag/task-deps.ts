import {
  DurableContext,
  TaskHandle,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (_event: unknown, context: DurableContext): Promise<number> => {
    let merge!: TaskHandle<"merge", number>;

    const result = await context.dag("diamond", (dag) => {
      const seed = dag.step("seed", [], async () => 10);
      const left = dag.step("left", [seed], async (deps) => deps["seed"] + 1);
      const right = dag.step("right", [seed], async (deps) => deps["seed"] * 2);

      merge = dag.step("merge", [left, right], async (deps) => {
        return deps["left"] + deps["right"];
      });
    });

    return result.getResult(merge)!;
  },
);
