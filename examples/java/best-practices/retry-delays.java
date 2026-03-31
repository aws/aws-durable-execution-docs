// Fast retry for transient network issues
var fastRetry = StepConfig.builder()
    .retryStrategy(RetryStrategies.exponentialBackoff(
        3,                        // max attempts
        Duration.ofSeconds(1),    // initial delay
        Duration.ofSeconds(5),    // max delay
        2.0,                      // backoff multiplier
        JitterStrategy.FULL))
    .build();

// Slow retry for rate limiting
var slowRetry = StepConfig.builder()
    .retryStrategy(RetryStrategies.exponentialBackoff(
        5,                        // max attempts
        Duration.ofSeconds(10),   // initial delay
        Duration.ofSeconds(60),   // max delay
        2.0,                      // backoff multiplier
        JitterStrategy.FULL))
    .build();
