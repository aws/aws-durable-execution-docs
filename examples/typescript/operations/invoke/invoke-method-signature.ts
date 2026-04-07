// Named overload
invoke<TInput, TOutput>(
  name: string,
  funcId: string,
  input?: TInput,
  config?: InvokeConfig<TInput, TOutput>,
): DurablePromise<TOutput>

// Unnamed overload
invoke<TInput, TOutput>(
  funcId: string,
  input?: TInput,
  config?: InvokeConfig<TInput, TOutput>,
): DurablePromise<TOutput>
