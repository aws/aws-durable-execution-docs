import {
  DurableContext,
  withDurableExecution,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: { id: string }, context: DurableContext) => {
    // Step 1: Fetch data — result is checkpointed
    const data = await context.step("fetch-data", async () => {
      return fetchData(event.id);
    });

    // Step 2: Wait 30 seconds without consuming compute resources
    await context.wait({ seconds: 30 });

    // Step 3: Process the data — only runs after the wait completes
    const result = await context.step("process-data", async () => {
      return processData(data);
    });

    return result;
  },
);

async function fetchData(id: string): Promise<string> {
  return `data-for-${id}`;
}

async function processData(data: string): Promise<string> {
  return `processed-${data}`;
}
