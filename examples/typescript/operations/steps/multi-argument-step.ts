import { DurableContext, withDurableExecution } from "@aws/durable-execution-sdk-js";

async function myStep(arg1: string, arg2: number): Promise<string> {
  return `${arg1}: ${arg2}`;
}

export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    const result = await context.step("my_step", async () => myStep("value", 42));
    return result;
  },
);
