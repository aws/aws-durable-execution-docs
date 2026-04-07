// sync
<T> T waitForCallback(String name, Class<T> resultType, BiConsumer<String, StepContext> func)
<T> T waitForCallback(String name, Class<T> resultType, BiConsumer<String, StepContext> func, WaitForCallbackConfig config)

// async
<T> DurableFuture<T> waitForCallbackAsync(String name, Class<T> resultType, BiConsumer<String, StepContext> func)
<T> DurableFuture<T> waitForCallbackAsync(String name, Class<T> resultType, BiConsumer<String, StepContext> func, WaitForCallbackConfig config)
