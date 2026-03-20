export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    // Start wait and step but don't await yet
    const waitPromise = context.wait("min-delay", { seconds: 5 });
    const stepPromise = context.step("process", async () => processData(event));

    // Wait for both — guarantees at least 5 seconds elapsed
    const [, result] = await Promise.all([waitPromise, stepPromise]);

    return result;
  },
);
