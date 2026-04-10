RetryStrategy RetryStrategies.exponentialBackoff(
    int maxAttempts,
    Duration initialDelay,
    Duration maxDelay,
    double backoffRate,
    JitterStrategy jitter
)

RetryStrategy RetryStrategies.fixedDelay(
    int maxAttempts,
    Duration fixedDelay
)
