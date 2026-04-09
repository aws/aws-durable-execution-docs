// Named overload
map<TInput, TOutput>(
  name: string | undefined,
  items: TInput[],
  mapFunc: MapFunc<TInput, TOutput>,
  config?: MapConfig<TInput, TOutput>,
): DurablePromise<BatchResult<TOutput>>

// Unnamed overload
map<TInput, TOutput>(
  items: TInput[],
  mapFunc: MapFunc<TInput, TOutput>,
  config?: MapConfig<TInput, TOutput>,
): DurablePromise<BatchResult<TOutput>>
