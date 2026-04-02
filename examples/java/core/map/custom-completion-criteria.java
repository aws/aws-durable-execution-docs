List<Integer> items = IntStream.range(0, 20)
    .boxed()
    .collect(Collectors.toList());

MapConfig config = MapConfig.builder()
    .completionConfig(CompletionConfig.builder()
        .minSuccessful(15)           // Succeed if at least 15 items succeed
        .toleratedFailureCount(5)    // Fail after 5 failures
        .build())
    .build();

MapResult<ProcessedItem> results = context.map(
    "process-items",
    items,
    (ctx, item, index) -> {
        // Processing that might fail
        if (item % 7 == 0) {
            throw new RuntimeException("Item " + item + " failed");
        }
        return new ProcessedItem(item, true);
    },
    config
);

return results.getResults();
