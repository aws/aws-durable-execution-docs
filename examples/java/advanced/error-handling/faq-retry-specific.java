var stepConfig = StepConfig.builder()
    .retryStrategy(RetryStrategies.exponentialBackoff(
        3,                        // max attempts
        Duration.ofSeconds(1),    // initial delay
        Duration.ofSeconds(10),   // max delay
        2.0,                      // backoff rate
        JitterStrategy.FULL))
    .build();
