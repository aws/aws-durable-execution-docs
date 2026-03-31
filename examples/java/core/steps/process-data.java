public class ProcessDataHandler extends DurableHandler<Map<String, String>, Map<String, String>> {

    @Override
    public Map<String, String> handleRequest(Map<String, String> event, DurableContext ctx) {
        // Create a retry strategy
        var stepConfig = StepConfig.builder()
            .retryStrategy(RetryStrategies.exponentialBackoff(
                3,                        // max attempts
                Duration.ofSeconds(1),    // initial delay
                Duration.ofSeconds(10),   // max delay
                2.0,                      // backoff rate
                JitterStrategy.FULL))
            .build();

        // Use the configuration
        var result = ctx.step("process-data", Map.class, stepCtx -> {
            // Your processing logic here
            return Map.of("processed", event.get("data"), "status", "completed");
        }, stepConfig);

        return result;
    }
}
