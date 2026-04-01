try {
    var callback = ctx.createCallback("approval", String.class,
        CallbackConfig.builder()
            .timeout(Duration.ofHours(1))
            .build());
    var result = callback.get();
    return Map.of("status", "approved");
} catch (CallbackFailedException e) {
    return Map.of("error", "CallbackFailedException", "callback_id", e.getCallbackId());
}
