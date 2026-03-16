def run_in_child_context(
    func: Callable[[DurableContext], T],
    name: str | None = None,
) -> T
