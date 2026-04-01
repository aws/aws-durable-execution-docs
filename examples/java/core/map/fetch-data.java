var urls = IntStream.range(0, 100)
    .mapToObj(i -> "https://example.com/api/" + i)
    .collect(Collectors.toList());

// Process only 5 URLs at a time
var config = MapConfig.builder()
    .maxConcurrency(5)
    .build();

var result = ctx.map("fetch-data", urls, Map.class,
    (url, index, childCtx) -> {
        // Network call that might be rate-limited
        return Map.of("url", url, "data", "...");
    },
    config);

return Map.of("results", result.results());
