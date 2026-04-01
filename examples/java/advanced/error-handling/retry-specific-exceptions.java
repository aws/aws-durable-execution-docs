var result = ctx.step("call-api", Map.class,
    stepCtx -> {
        // Call external API that might fail
        return callExternalApi();
    },
    StepConfig.builder()
        .retryStrategy(RetryStrategies.exponentialBackoff(
            3,                        // max attempts
            Duration.ofSeconds(1),    // initial delay
            Duration.ofSeconds(10),   // max delay
            2.0,                      // backoff multiplier
            JitterStrategy.FULL))     // randomize delays
        .build());
