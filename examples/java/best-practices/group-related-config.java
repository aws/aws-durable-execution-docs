// StepConfigs.java
var FAST_RETRY = StepConfig.builder()
    .retryStrategy(RetryStrategies.exponentialBackoff(
        3,                        // max attempts
        Duration.ofSeconds(1),    // initial delay
        Duration.ofSeconds(5),    // max delay
        2.0,                      // backoff multiplier
        JitterStrategy.FULL))
    .build();

// Handler.java
var data = ctx.step("fetch_data", Map.class,
    stepCtx -> fetchData(input.get("id")),
    FAST_RETRY);
return data;
