export const handler = withDurableExecution(
  async (event: any, context: DurableContext) => {
    const result = await context.waitForCondition(
      async (state: { jobId: string; status: string }) => {
        const status = await getJobStatus(state.jobId);
        return { ...state, status };
      },
      {
        initialState: { jobId: "job-123", status: "pending" },
        waitStrategy: (state, attempt) => {
          if (state.status === "completed") {
            return { shouldContinue: false };
          }
          return { shouldContinue: true, delay: { seconds: 60 } };
        },
      },
    );
    return result;
  },
);
