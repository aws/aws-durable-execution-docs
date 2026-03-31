// Expected 2 minutes + 1 minute buffer = 3 minutes
var callback = ctx.createCallback("approval", Map.class,
    CallbackConfig.builder()
        .timeout(Duration.ofMinutes(3))
        .build());
return Map.of("callback_id", callback.getCallbackId());
