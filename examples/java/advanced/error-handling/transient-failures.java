// Use transient preset for quick retries
var stepConfig = StepConfig.builder()
    .retryStrategy(RetryStrategies.Presets.TRANSIENT)
    .build();

try {
    var result = ctx.step("call_external_api", Map.class, stepCtx -> {
        // API call that might fail transiently
        return makeHttpRequest((String) event.get("endpoint"));
    }, stepConfig);
    return Map.of("status", "success", "data", result);
} catch (Exception e) {
    // All retries exhausted
    return Map.of("status", "failed", "error", e.getMessage());
}
