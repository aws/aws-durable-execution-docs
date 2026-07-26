import {
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (_event: unknown, context: DurableContext): Promise<number> => {
    const result = await context.dag("mixed-kinds", (dag) => {
      dag.step("compute", [], async () => 1);

      dag.map("square-each", [], [1, 2, 3], async (ctx, item, index) =>
        ctx.step(`square-${index}`, async () => item * item),
      );

      dag.wait("cooldown", [], { seconds: 1 });

      dag.runInChildContext("group", [], async (ctx) =>
        ctx.step("inner", async () => "done"),
      );
    });

    return result.successCount;
  },
);
