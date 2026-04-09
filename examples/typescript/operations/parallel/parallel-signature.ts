// Named overload
parallel<TOutput>(
  name: string | undefined,
  branches: (ParallelFunc<TOutput> | NamedParallelBranch<TOutput>)[],
  config?: ParallelConfig<TOutput>,
): DurablePromise<BatchResult<TOutput>>

// Unnamed overload
parallel<TOutput>(
  branches: (ParallelFunc<TOutput> | NamedParallelBranch<TOutput>)[],
  config?: ParallelConfig<TOutput>,
): DurablePromise<BatchResult<TOutput>>
