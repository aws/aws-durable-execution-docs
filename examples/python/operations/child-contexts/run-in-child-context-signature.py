def run_in_child_context(
    self,
    func: Callable[[DurableContext], T],
    name: str | None = None,
    config: ChildConfig | None = None,
) -> T: ...
