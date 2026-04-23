const outcome = await context.waitForCallback(
  "long-running-job",
  async (callbackId) => startJob({ jobId: event.jobId, callbackId }),
  {
    timeout: { hours: 24 },
    heartbeatTimeout: { minutes: 10 },
  },
);
