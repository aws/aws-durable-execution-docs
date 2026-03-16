# Ensure you're retrying the right errors
retry_config = RetryStrategyConfig(
    max_attempts=5,  # Increase attempts
    retryable_error_types=[ConnectionError, TimeoutError],  # Add error types
)
