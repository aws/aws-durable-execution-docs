// sync
<T, U> T invoke(String name, String functionName, U payload, Class<T> resultType)
<T, U> T invoke(String name, String functionName, U payload, Class<T> resultType, InvokeConfig config)
<T, U> T invoke(String name, String functionName, U payload, TypeToken<T> resultType)
<T, U> T invoke(String name, String functionName, U payload, TypeToken<T> resultType, InvokeConfig config)

// async
<T, U> DurableFuture<T> invokeAsync(String name, String functionName, U payload, Class<T> resultType)
<T, U> DurableFuture<T> invokeAsync(String name, String functionName, U payload, Class<T> resultType, InvokeConfig config)
<T, U> DurableFuture<T> invokeAsync(String name, String functionName, U payload, TypeToken<T> resultType)
<T, U> DurableFuture<T> invokeAsync(String name, String functionName, U payload, TypeToken<T> resultType, InvokeConfig config)
