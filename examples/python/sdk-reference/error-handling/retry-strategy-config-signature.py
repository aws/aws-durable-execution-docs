import re
from dataclasses import dataclass
from aws_durable_execution_sdk_python.config import Duration, JitterStrategy
from aws_durable_execution_sdk_python.retries import RetryDecision


@dataclass
class RetryStrategyConfig:
    max_attempts: int = 3
    initial_delay: Duration = Duration.from_seconds(5)
    max_delay: Duration = Duration.from_minutes(5)
    backoff_rate: float = 2.0
    jitter_strategy: JitterStrategy = JitterStrategy.FULL
    retryable_errors: list[str | re.Pattern] | None = None
    retryable_error_types: list[type[Exception]] | None = None
