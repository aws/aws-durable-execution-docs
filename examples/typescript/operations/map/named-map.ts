import {
  BatchResult,
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: { userIds: string[] }, context: DurableContext): Promise<string[]> => {
    // Named: pass name as first argument, undefined to omit
    const result: BatchResult<string> = await context.map(
      "process-users",
      event.userIds,
      async (ctx, userId, index) =>
        ctx.step(`process-${index}`, async () => `processed-${userId}`),
    );

    return result.getResults();
  },
);
