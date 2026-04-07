def wait_for_callback(
    self,
    submitter: Callable[[str, WaitForCallbackContext], None],
    name: str | None = None,
    config: WaitForCallbackConfig | None = None,
) -> Any: ...
