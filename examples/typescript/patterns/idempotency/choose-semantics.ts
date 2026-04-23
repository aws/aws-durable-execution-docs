import { StepSemantics } from "@aws/durable-execution-sdk-js";

// At-least-once (default) for a retryable idempotent write.
await context.step("upsert-user", async () => {
  return userStore.upsert(event.user);
});

// At-most-once for a side-effecting call, with retries disabled.
await context.step(
  "charge-payment",
  async () => paymentService.charge(event.amount, event.cardToken),
  {
    semantics: StepSemantics.AtMostOncePerRetry,
    retryStrategy: () => ({ shouldRetry: false }),
  },
);
