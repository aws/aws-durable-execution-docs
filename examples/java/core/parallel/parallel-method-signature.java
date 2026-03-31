// Async child context (parallel branch)
<T> DurableFuture<T> runInChildContextAsync(
    String name,
    Class<T> resultType,
    Function<DurableContext, T> func
)

// Wait for multiple futures
static DurableFuture<Void> DurableFuture.allOf(DurableFuture<?>... futures)

// Map-based parallel (data-driven)
<I, O> MapResult<O> map(
    String name,
    Collection<I> items,
    Class<O> resultType,
    MapFunction<I, O> func,
    MapConfig config
)
