retry_config = RetryStrategyConfig(
    max_attempts=3,
    retryable_error_types=[ConnectionError, TimeoutError],
)
