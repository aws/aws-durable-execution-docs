from collections.abc import Callable
from dataclasses import dataclass
from aws_durable_execution_sdk_python.config import Duration

# retry_strategy: Callable[[Exception, int], RetryDecision]
# attempt_count is one-indexed: 1 on the first retry, 2 on the second, etc.


@dataclass
class RetryDecision:
    should_retry: bool
    delay: Duration
    pause: bool = False  # Experimental

    @classmethod
    def retry(cls, delay: Duration, *, pause: bool = False) -> "RetryDecision": ...

    @classmethod
    def no_retry(cls) -> "RetryDecision": ...
