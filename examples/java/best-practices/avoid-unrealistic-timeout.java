// Too short - will timeout before external system responds
var callback = ctx.createCallback("approval", Map.class,
    CallbackConfig.builder()
        .timeout(Duration.ofSeconds(5))
        .build());
return Map.of("callback_id", callback.getCallbackId());
