// Create callback
var callback = ctx.createCallback("external_api", Map.class);

// Send request
ctx.step("send_request", Void.class,
    stepCtx -> {
        sendExternalRequest(callback.callbackId());
        return null;
    });

// Wait with retry
var stepConfig = StepConfig.builder()
    .retryStrategy(RetryStrategies.exponentialBackoff(
        3,                        // max attempts
        Duration.ofSeconds(5),    // initial delay
        Duration.ofSeconds(30),   // max delay
        2.0,                      // backoff rate
        JitterStrategy.FULL))
    .build();

var result = ctx.step("wait_for_response", Map.class,
    stepCtx -> {
        // Wait for callback result
        return callback.get();
    }, stepConfig);

return result;
