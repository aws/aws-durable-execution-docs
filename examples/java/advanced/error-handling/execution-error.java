var result = ctx.step("process-data", Map.class, stepCtx -> {
    var data = (Map<String, Object>) event.get("data");
    if (!data.containsKey("required_field")) {
        throw new RuntimeException("Required field missing");
    }
    return Map.of("processed", true);
});
