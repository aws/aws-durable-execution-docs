public String handleRequest(Map<String, Object> event, DurableContext ctx) {
    var stepConfig = StepConfig.builder()
        .retryStrategy(RetryStrategies.exponentialBackoff(
            3,                        // max attempts
            Duration.ofSeconds(1),    // initial delay
            Duration.ofSeconds(10),   // max delay
            2.0,                      // backoff rate
            JitterStrategy.FULL))
        .build();

    var result = ctx.step("unreliable-operation", String.class, stepCtx -> {
        // Operation that might fail
        if (Math.random() > 0.5) {
            throw new RuntimeException("Random error occurred");
        }
        return "Operation succeeded";
    }, stepConfig);

    return result;
}
