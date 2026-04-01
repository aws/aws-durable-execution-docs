try {
    // SDK raises exception if configuration is invalid
    var callback = ctx.createCallback("approval", String.class,
        CallbackConfig.builder()
            .timeout(Duration.ofSeconds(-1))  // Invalid!
            .build());
    return Map.of("callback_id", callback.callbackId());
} catch (IllegalArgumentException e) {
    // SDK caught invalid configuration
    return Map.of("error", "InvalidConfiguration", "message", e.getMessage());
}
