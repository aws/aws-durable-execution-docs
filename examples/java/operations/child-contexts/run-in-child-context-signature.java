// sync
<T> T runInChildContext(String name, Class<T> resultType, Function<DurableContext, T> func)
<T> T runInChildContext(String name, Class<T> resultType, Function<DurableContext, T> func, RunInChildContextConfig config)
<T> T runInChildContext(String name, TypeToken<T> resultType, Function<DurableContext, T> func)
<T> T runInChildContext(String name, TypeToken<T> resultType, Function<DurableContext, T> func, RunInChildContextConfig config)

// async
<T> DurableFuture<T> runInChildContextAsync(String name, Class<T> resultType, Function<DurableContext, T> func)
<T> DurableFuture<T> runInChildContextAsync(String name, Class<T> resultType, Function<DurableContext, T> func, RunInChildContextConfig config)
<T> DurableFuture<T> runInChildContextAsync(String name, TypeToken<T> resultType, Function<DurableContext, T> func)
<T> DurableFuture<T> runInChildContextAsync(String name, TypeToken<T> resultType, Function<DurableContext, T> func, RunInChildContextConfig config)
