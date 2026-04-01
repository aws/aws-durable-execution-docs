var customerTier = (String) event.getOrDefault("tier", "standard");
var data = (Map<String, Object>) event.get("data");

Map<String, Object> result;

if ("premium".equals(customerTier)) {
    result = ctx.runInChildContext("premium_processing", Map.class, childCtx -> {
        var enhanced = childCtx.step("enhance", Map.class,
            stepCtx -> enhanceData(data));
        var validated = childCtx.step("validate", Map.class,
            stepCtx -> validatePremium(enhanced));
        var processed = childCtx.step("process", Map.class,
            stepCtx -> processPremium(validated));
        return Map.of("type", "premium", "result", processed);
    });
} else {
    result = ctx.runInChildContext("standard_processing", Map.class, childCtx -> {
        var processed = childCtx.step("process", Map.class,
            stepCtx -> processStandard(data));
        return Map.of("type", "standard", "result", processed);
    });
}

return result;
