import { retryPresets, StepSemantics } from "@aws/durable-execution-sdk-js";

const paymentStepConfig = {
  semantics: StepSemantics.AtMostOncePerRetry,
  retryStrategy: () => ({ shouldRetry: false }),
};

const idempotentStepConfig = {
  retryStrategy: retryPresets.default,
};

await context.step("charge", () => chargePayment(order), paymentStepConfig);
await context.step("refund", () => refundPayment(order), paymentStepConfig);
await context.step("fetch-user", () => userStore.get(id), idempotentStepConfig);
