import {
  BatchResult,
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: any, context: DurableContext): Promise<string[]> => {
    const items = ["a", "b", "c"];

    const result: BatchResult<string> = await context.parallel(
      "process-items",
      items.map((item) => async (ctx: DurableContext) =>
        ctx.step(`process-${item}`, async () => `processed ${item}`),
      ),
    );

    return result.getResults();
  },
);
