// sync — blocks until all items complete
<I, O> MapResult<O> map(String name, Collection<I> items, Class<O> resultType,
                        MapFunction<I, O> function)
<I, O> MapResult<O> map(String name, Collection<I> items, Class<O> resultType,
                        MapFunction<I, O> function, MapConfig config)
<I, O> MapResult<O> map(String name, Collection<I> items, TypeToken<O> resultType,
                        MapFunction<I, O> function)
<I, O> MapResult<O> map(String name, Collection<I> items, TypeToken<O> resultType,
                        MapFunction<I, O> function, MapConfig config)

// async — returns immediately
<I, O> DurableFuture<MapResult<O>> mapAsync(String name, Collection<I> items,
                                            Class<O> resultType, MapFunction<I, O> function)
<I, O> DurableFuture<MapResult<O>> mapAsync(String name, Collection<I> items,
                                            Class<O> resultType, MapFunction<I, O> function,
                                            MapConfig config)
<I, O> DurableFuture<MapResult<O>> mapAsync(String name, Collection<I> items,
                                            TypeToken<O> resultType, MapFunction<I, O> function)
<I, O> DurableFuture<MapResult<O>> mapAsync(String name, Collection<I> items,
                                            TypeToken<O> resultType, MapFunction<I, O> function,
                                            MapConfig config)
