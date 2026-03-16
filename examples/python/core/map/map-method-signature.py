def map(
    inputs: Sequence[U],
    func: Callable[[DurableContext, U, int, Sequence[U]], T],
    name: str | None = None,
    config: MapConfig | None = None,
) -> BatchResult[T]
