// Try primary service
var primaryCallback = ctx.createCallback("primary_service", Map.class,
    CallbackConfig.builder()
        .timeout(Duration.ofSeconds(30))
        .build());

ctx.step("send_to_primary", Void.class, stepCtx -> {
    sendToPrimaryService(primaryCallback.callbackId(), event.get("data"));
    return null;
});

Map<String, Object> result;
try {
    result = primaryCallback.get();
    return Map.of("result", result, "source", "primary");
} catch (CallbackTimeoutException e) {
    // Primary service timed out, use fallback
    var fallbackCallback = ctx.createCallback("fallback_service", Map.class,
        CallbackConfig.builder()
            .timeout(Duration.ofMinutes(2))
            .build());

    ctx.step("send_to_fallback", Void.class, stepCtx -> {
        sendToFallbackService(fallbackCallback.callbackId(), event.get("data"));
        return null;
    });

    result = fallbackCallback.get();
    return Map.of("result", result, "source", "fallback");
}
