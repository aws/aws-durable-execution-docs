public String handleRequest(Map<String, Object> event, DurableContext ctx) {
    // Configure exponential backoff
    var stepConfig = StepConfig.builder()
        .retryStrategy(RetryStrategies.exponentialBackoff(
            3,                        // max attempts
            Duration.ofSeconds(1),    // initial delay
            Duration.ofSeconds(10),   // max delay
            2.0,                      // backoff rate
            JitterStrategy.FULL))
        .build();

    var result = ctx.step("retry-step", String.class, stepCtx -> {
        return "Step with exponential backoff";
    }, stepConfig);

    return "Result: " + result;
}
