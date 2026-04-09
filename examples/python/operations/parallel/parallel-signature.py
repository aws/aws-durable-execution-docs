def parallel(
    self,
    functions: Sequence[Callable[[DurableContext], T]],
    name: str | None = None,
    config: ParallelConfig | None = None,
) -> BatchResult[T]: ...
