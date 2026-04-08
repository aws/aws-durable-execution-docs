// Named overload
runInChildContext<TOutput>(
  name: string | undefined,
  fn: (context: DurableContext) => Promise<TOutput>,
  config?: ChildConfig<TOutput>,
): DurablePromise<TOutput>;

// Unnamed overload
runInChildContext<TOutput>(
  fn: (context: DurableContext) => Promise<TOutput>,
  config?: ChildConfig<TOutput>,
): DurablePromise<TOutput>;
