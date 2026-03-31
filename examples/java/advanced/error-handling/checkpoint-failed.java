// Reduce payload size by returning only necessary data
var summary = ctx.step("large-operation", Map.class, stepCtx -> {
    // Process large data
    var largeResult = processData();

    // Return only summary, not full data
    return Map.of("summary", largeResult.get("summary"), "count", largeResult.get("items").size());
});
