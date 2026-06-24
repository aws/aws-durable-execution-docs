import {
  DurableContext,
  withDurableExecution,
  createFileSystemSerdes,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: { customerId: string }, context: DurableContext) => {
    const fsSerdes = createFileSystemSerdes("/mnt/s3");

    // Pass the FileSystem serdes to one step. Other operations in this handler
    // keep using the default JSON serdes.
    const profile = await context.step(
      "load-profile",
      async () => loadLargeProfile(event.customerId),
      { serdes: fsSerdes },
    );

    return { name: profile.name };
  },
);

declare function loadLargeProfile(
  customerId: string,
): Promise<{ name: string }>;
