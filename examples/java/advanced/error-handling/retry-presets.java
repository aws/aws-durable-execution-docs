// No retries
var noRetry = StepConfig.builder()
    .retryStrategy(RetryStrategies.Presets.NO_RETRY)
    .build();

// Default retries (6 attempts, 5s initial delay)
var defaultRetry = StepConfig.builder()
    .retryStrategy(RetryStrategies.Presets.DEFAULT)
    .build();
