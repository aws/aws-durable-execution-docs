def step(
    func: Callable[[StepContext], T],
    name: str | None = None,
    config: StepConfig | None = None,
) -> T
