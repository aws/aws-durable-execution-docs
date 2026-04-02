import {
  DurableContext,
  StepContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

async function addNumbers(ctx: StepContext, a: number, b: number): Promise<number> {
  return a + b;
}

export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    const result = await context.step("add_numbers", (ctx) => addNumbers(ctx, 5, 3));
    return result;
  },
);
