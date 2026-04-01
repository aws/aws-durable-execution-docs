var callback = ctx.createCallback("conditional_callback", Map.class,
    CallbackConfig.builder()
        .timeout(Duration.ofMinutes(10))
        .build());

// Send request
ctx.step("send_request", Void.class, stepCtx -> {
    sendRequest(callback.callbackId(), event.get("request_type"));
    return null;
});

// Wait for result
Map<String, Object> result;
try {
    result = callback.get();
} catch (CallbackTimeoutException e) {
    return Map.of("status", "timeout", "message", "No response received");
}

// Handle different result types
var resultType = (String) result.get("type");

if ("success".equals(resultType)) {
    return processSuccess(result);
} else if ("partial".equals(resultType)) {
    return processPartial(result);
} else {
    return processFailure(result);
}
