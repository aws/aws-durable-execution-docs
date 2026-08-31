# context.wait_for_condition()
def wait_for_condition(
    self,
    check: Callable[[T, WaitForConditionCheckContext], T],
    config: WaitForConditionConfig[T],
    name: str | None = None,
) -> T

# Check function
Callable[[T, WaitForConditionCheckContext], T]

# WaitForConditionCheckContext — passed to each check
class WaitForConditionCheckContext:
    logger: LoggerInterface
    attempt: int  # current attempt number, starting at 1

# Config
@dataclass
class WaitForConditionConfig(Generic[T]):
    wait_strategy: Callable[[T, int], WaitForConditionDecision]
    initial_state: T
    serdes: SerDes | None = None
