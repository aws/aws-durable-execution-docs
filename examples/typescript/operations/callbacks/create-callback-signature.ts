// Named overload
createCallback<TOutput = string>(
  name: string | undefined,
  config?: CreateCallbackConfig<TOutput>,
): DurablePromise<[DurablePromise<TOutput>, string]>;

// Anonymous overload
createCallback<TOutput = string>(
  config?: CreateCallbackConfig<TOutput>,
): DurablePromise<[DurablePromise<TOutput>, string]>;
