import {
  withDurableExecution,
  StepSemantics,
  StepInterruptedError,
} from "@aws/durable-execution-sdk-js";

export const handler = withDurableExecution(async (event, context) => {
  try {
    const result = await context.step(
      "charge-payment",
      async (stepCtx) => {
        return chargePayment((event as { amount: number }).amount);
      },
      { semantics: StepSemantics.AtMostOncePerRetry },
    );
    return { status: "charged", result };
  } catch (err) {
    if (err instanceof StepInterruptedError) {
      // The step started but Lambda was interrupted before the result was
      // checkpointed. The SDK will not re-run the step on the next invocation.
      // Inspect your payment system to determine whether the charge succeeded.
      context.logger.warn("Payment step interrupted — check payment system");
      return { status: "unknown" };
    }
    throw err;
  }
});

function chargePayment(amount: number): { charged: number } {
  return { charged: amount };
}
