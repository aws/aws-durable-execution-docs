# Wait strategy
Callable[[T, int], WaitForConditionDecision]

# Decision
@dataclass
class WaitForConditionDecision:
    should_continue: bool
    delay: Duration

    @classmethod
    def continue_waiting(cls, delay: Duration) -> WaitForConditionDecision: ...

    @classmethod
    def stop_polling(cls) -> WaitForConditionDecision: ...
