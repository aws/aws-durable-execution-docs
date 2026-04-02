// Sync (blocks until complete)
<T> T step(String name, Class<T> resultType, Function<StepContext, T> func)
<T> T step(String name, Class<T> resultType, Function<StepContext, T> func, StepConfig config)

// Async (returns a DurableFuture)
<T> DurableFuture<T> stepAsync(String name, Class<T> resultType, Function<StepContext, T> func)
<T> DurableFuture<T> stepAsync(String name, Class<T> resultType, Function<StepContext, T> func, StepConfig config)
