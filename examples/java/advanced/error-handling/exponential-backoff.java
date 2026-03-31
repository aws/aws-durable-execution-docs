var stepConfig = StepConfig.builder()
    .retryStrategy(RetryStrategies.exponentialBackoff(
        5,                         // max attempts
        Duration.ofSeconds(1),     // first retry after 1 second
        Duration.ofSeconds(60),    // cap at 60 seconds
        2.0,                       // double delay each time: 1s, 2s, 4s, 8s, 16s...
        JitterStrategy.FULL))
    .build();
