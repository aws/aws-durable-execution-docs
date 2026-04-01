var items = (List<String>) event.get("items");

// Process at most 10 items concurrently using map
var config = MapConfig.builder()
    .maxConcurrency(10)
    .build();

var result = ctx.map("process-items", items, String.class,
    (item, index, childCtx) ->
        childCtx.step("process-" + index, String.class,
            stepCtx -> "Processed " + item),
    config);

return Map.of(
    "processed", result.succeeded().size(),
    "failed_count", result.failed().size(),
    "results", result.results());
