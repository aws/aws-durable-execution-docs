import { createWaitStrategy } from "@aws/durable-execution-sdk-js";

const strategy = createWaitStrategy({
  shouldContinuePolling: (state) => state.status !== "COMPLETED",
  maxAttempts: 10,
  initialDelay: { seconds: 5 },
  maxDelay: { minutes: 5 },
  backoffRate: 2.0,
  jitter: JitterStrategy.FULL,
});
