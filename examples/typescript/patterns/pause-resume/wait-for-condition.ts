const final = await context.waitForCondition(
  "wait-for-job",
  async (state, ctx) => {
    const status = await jobService.getStatus(state.jobId);
    return { ...state, status };
  },
  {
    initialState: { jobId: event.jobId, status: "pending" },
    waitStrategy: (state, attempt) => {
      if (state.status === "completed") return { shouldContinue: false };
      const delaySeconds = Math.min(2 ** attempt, 60);
      return { shouldContinue: true, delay: { seconds: delaySeconds } };
    },
  },
);
