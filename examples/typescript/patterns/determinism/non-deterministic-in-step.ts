import { withDurableExecution, DurableContext } from "@aws/durable-execution-sdk-js";
import { randomUUID } from "crypto";

export const handler = withDurableExecution(
  async (event: { amount: number }, context: DurableContext) => {
    const transactionId = await context.step(
      "generate-transaction-id",
      async () => randomUUID(),
    );

    const receipt = await context.step("charge", async () => {
      return charge(event.amount, transactionId);
    });

    return { transactionId, receipt };
  },
);
