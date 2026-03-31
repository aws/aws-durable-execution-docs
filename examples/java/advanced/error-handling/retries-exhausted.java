// Ensure you're retrying the right errors
var stepConfig = StepConfig.builder()
    .retryStrategy(RetryStrategies.exponentialBackoff(
        5,                        // increase attempts
        Duration.ofSeconds(1),    // initial delay
        Duration.ofSeconds(30),   // max delay
        2.0,                      // backoff rate
        JitterStrategy.FULL))
    .build();
