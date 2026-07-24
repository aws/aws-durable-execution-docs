// context.waitForCondition()
waitForCondition<T>(
  name: string | undefined,
  checkFunc: WaitForConditionCheckFunc<T>,
  config: WaitForConditionConfig<T>,
): DurablePromise<T>;

waitForCondition<T>(
  checkFunc: WaitForConditionCheckFunc<T>,
  config: WaitForConditionConfig<T>,
): DurablePromise<T>;

// Check function
type WaitForConditionCheckFunc<T> = (
  state: T,
  context: WaitForConditionContext,
) => Promise<T>;

// WaitForConditionContext — provides a logger and the 1-based attempt number
interface WaitForConditionContext {
  logger: DurableLogger;
  attempt: number;
}

// Config
interface WaitForConditionConfig<T> {
  waitStrategy: WaitForConditionWaitStrategyFunc<T>;
  initialState: T;
  serdes?: Serdes<T>;
}
