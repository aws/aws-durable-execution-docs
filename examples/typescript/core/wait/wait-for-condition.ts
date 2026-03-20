export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    // Poll until job completes
    const result = await context.waitForCondition(
      "wait_for_job",
      async (state: { jobId: string; status: string; done: boolean }) => {
        const status = await getJobStatus(state.jobId);
        return { ...state, status, done: status === "COMPLETED" };
      },
      {
        initialState: { jobId: "job-123", status: "pending", done: false },
        waitStrategy: (state, attempt) => {
          if (state.done) {
            return { shouldContinue: false };
          }
          return {
            shouldContinue: true,
            delay: { seconds: Math.min(5 * Math.pow(2, attempt), 300) },
          };
        },
      },
    );
    return result;
  },
);
