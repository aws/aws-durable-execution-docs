<I, O> MapResult<O> map(
    String name,
    Collection<I> items,
    Class<O> resultType,
    MapFunction<I, O> func
)

<I, O> MapResult<O> map(
    String name,
    Collection<I> items,
    Class<O> resultType,
    MapFunction<I, O> func,
    MapConfig config
)
