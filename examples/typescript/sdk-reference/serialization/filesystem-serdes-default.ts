import {
  DurableContext,
  withDurableExecution,
  createFileSystemSerdes,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(
  async (event: { customerId: string }, context: DurableContext) => {
    // Configure once. Every step result, child-context result, invoke result,
    // and waitForCondition result in this handler is now stored on /mnt/s3.
    context.configureSerdes({
      defaultSerdes: createFileSystemSerdes("/mnt/s3"),
    });

    const profile = await context.step("load-profile", async () => {
      return loadLargeProfile(event.customerId);
    });

    return { name: profile.name };
  },
);

declare function loadLargeProfile(
  customerId: string,
): Promise<{ name: string }>;
