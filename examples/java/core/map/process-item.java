var items = IntStream.range(0, 100).boxed().collect(Collectors.toList());

// Configure map operation
var config = MapConfig.builder()
    .maxConcurrency(10)  // Process 10 items at a time
    .completionConfig(CompletionConfig.allSuccessful())  // Require all to succeed
    .build();

var result = ctx.map("process_numbers", items, Map.class,
    (item, index, childCtx) -> Map.of("item", item, "squared", item * item),
    config);

return Map.of("results", result.results());
