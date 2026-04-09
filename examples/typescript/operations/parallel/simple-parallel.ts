import {
  BatchResult,
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: any, context: DurableContext): Promise<string[]> => {
    const result: BatchResult<string> = await context.parallel("check-services", [
      async (ctx) => ctx.step("check-inventory", async () => "inventory ok"),
      async (ctx) => ctx.step("check-payment", async () => "payment ok"),
      async (ctx) => ctx.step("check-shipping", async () => "shipping ok"),
    ]);

    return result.getResults();
  },
);
