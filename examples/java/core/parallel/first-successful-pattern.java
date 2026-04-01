// Try multiple data sources, use first successful
var primaryFuture = ctx.runInChildContextAsync("try-primary-db", Map.class,
    child -> child.step("query", Map.class,
        stepCtx -> Map.of("source", "primary", "data", "...")));

var secondaryFuture = ctx.runInChildContextAsync("try-secondary-db", Map.class,
    child -> child.step("query", Map.class,
        stepCtx -> Map.of("source", "secondary", "data", "...")));

var cacheFuture = ctx.runInChildContextAsync("try-cache", Map.class,
    child -> child.step("query", Map.class,
        stepCtx -> Map.of("source", "cache", "data", "...")));

// Wait for all to complete
DurableFuture.allOf(primaryFuture, secondaryFuture, cacheFuture);

// Use the first successful result
return primaryFuture.get();
