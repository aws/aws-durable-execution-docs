retry_config = RetryStrategyConfig(
    max_attempts=5,
    initial_delay_seconds=1,    # First retry after 1 second
    max_delay_seconds=60,        # Cap at 60 seconds
    backoff_rate=2.0,            # Double delay each time: 1s, 2s, 4s, 8s, 16s...
)
