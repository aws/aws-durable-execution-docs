// Good - only checkpoint what you need
var result = ctx.step("process-data", Map.class, stepCtx -> {
    return processData(Map.of("id", order.getId(), "amount", order.getAmount()));
});

// Avoid - large objects in checkpoints
var result = ctx.step("process-data", Map.class, stepCtx -> {
    return processData(entireDatabaseDump);  // Too large
});
