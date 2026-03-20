export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    // Start a process
    const jobId = await context.step("start_job", async () => startJob());

    // Wait before checking status
    await context.wait("initial_delay", { seconds: 30 });

    // Check status
    const status = await context.step("check_status", async () =>
      checkJobStatus(jobId),
    );

    return { jobId, status };
  },
);
