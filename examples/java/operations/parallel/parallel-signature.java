// Create a parallel operation (returns ParallelDurableFuture)
ParallelDurableFuture parallel(String name)
ParallelDurableFuture parallel(String name, ParallelConfig config)

// Register a branch on the ParallelDurableFuture
<T> DurableFuture<T> branch(String name, Class<T> resultType, Function<DurableContext, T> func)
<T> DurableFuture<T> branch(String name, TypeToken<T> resultType, Function<DurableContext, T> func)
<T> DurableFuture<T> branch(String name, Class<T> resultType, Function<DurableContext, T> func, ParallelBranchConfig config)

// Block until all branches complete (also called by close())
ParallelResult get()
