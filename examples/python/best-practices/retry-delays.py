from aws_durable_execution_sdk_python.retries import RetryStrategyConfig

# Fast retry for transient network issues
fast_retry = RetryStrategyConfig(
    max_attempts=3,
    initial_delay_seconds=1,
    max_delay_seconds=5,
    backoff_rate=2.0,
)

# Slow retry for rate limiting
slow_retry = RetryStrategyConfig(
    max_attempts=5,
    initial_delay_seconds=10,
    max_delay_seconds=60,
    backoff_rate=2.0,
)
