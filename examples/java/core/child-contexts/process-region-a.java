var data = (Map<String, Object>) event.get("data");

// Execute child contexts sequentially for each region
var resultA = ctx.runInChildContext("region_a", Map.class, childCtx -> {
    var result = childCtx.step("process_a", Map.class,
        stepCtx -> processForRegion("A", data));
    return Map.of("region", "A", "result", result);
});

var resultB = ctx.runInChildContext("region_b", Map.class, childCtx -> {
    var result = childCtx.step("process_b", Map.class,
        stepCtx -> processForRegion("B", data));
    return Map.of("region", "B", "result", result);
});

var resultC = ctx.runInChildContext("region_c", Map.class, childCtx -> {
    var result = childCtx.step("process_c", Map.class,
        stepCtx -> processForRegion("C", data));
    return Map.of("region", "C", "result", result);
});

return Map.of(
    "regions_processed", 3,
    "results", List.of(resultA, resultB, resultC));
