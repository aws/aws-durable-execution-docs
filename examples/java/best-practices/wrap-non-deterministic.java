var timestamp = ctx.step("get_timestamp", Long.class,
    stepCtx -> Instant.now().getEpochSecond());
var requestId = ctx.step("generate_id", String.class,
    stepCtx -> UUID.randomUUID().toString());
return Map.of("timestamp", timestamp, "request_id", requestId);
