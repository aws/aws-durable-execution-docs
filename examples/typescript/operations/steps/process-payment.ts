import {
  DurableContext,
  StepContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

async function processPayment(ctx: StepContext, amount: number): Promise<object> {
  return { status: "completed", amount };
}

export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    // Step is automatically named "processPayment"
    const result = await context.step("process_payment", (ctx) =>
      processPayment(ctx, 100.0),
    );
    return result;
  },
);
