import {
  BatchResult,
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

// Plain function branch
async function taskA(ctx: DurableContext): Promise<string> {
  return ctx.step("run-a", async () => "a done");
}

export const handler = withDurableExecution(
  async (event: any, context: DurableContext): Promise<string[]> => {
    const result: BatchResult<string> = await context.parallel("process", [
      // ParallelFunc: plain async function
      taskA,
      // NamedParallelBranch: object with name and func
      { name: "task-b", func: async (ctx) => ctx.step("run-b", async () => "b done") },
    ]);

    return result.getResults();
  },
);
