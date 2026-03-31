<T> T runInChildContext(
    String name,
    Class<T> resultType,
    Function<DurableContext, T> func
)

<T> DurableFuture<T> runInChildContextAsync(
    String name,
    Class<T> resultType,
    Function<DurableContext, T> func
)
