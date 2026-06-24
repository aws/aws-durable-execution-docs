import re
from dataclasses import dataclass
from aws_durable_execution_sdk_python.config import Duration, JitterStrategy


@dataclass
class LinearRetryStrategyConfig:
    max_attempts: int = 6
    initial_delay: Duration = Duration.from_seconds(1)
    increment: Duration = Duration.from_seconds(1)
    max_delay: Duration = Duration.from_minutes(5)
    jitter_strategy: JitterStrategy = JitterStrategy.FULL
    retryable_errors: list[str | re.Pattern] | None = None
    retryable_error_types: list[type[Exception]] | None = None
