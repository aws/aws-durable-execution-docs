// With Class<T>
<T> DurableCallbackFuture<T> createCallback(String name, Class<T> resultType)
<T> DurableCallbackFuture<T> createCallback(String name, Class<T> resultType, CallbackConfig config)

// With TypeToken<T> for generic result types
<T> DurableCallbackFuture<T> createCallback(String name, TypeToken<T> resultType)
<T> DurableCallbackFuture<T> createCallback(String name, TypeToken<T> resultType, CallbackConfig config)
