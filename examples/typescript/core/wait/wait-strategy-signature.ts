// Wait strategy
type WaitForConditionWaitStrategyFunc<T> = (
  state: T,
  attempt: number,
) => WaitForConditionDecision;

// Decision
type WaitForConditionDecision =
  | { shouldContinue: true; delay: Duration }
  | { shouldContinue: false };
