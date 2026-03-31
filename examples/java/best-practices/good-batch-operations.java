var items = (List<Map<String, Object>>) input.get("items");
var results = new ArrayList<>();

// Process 10 items per step instead of 1
for (int i = 0; i < items.size(); i += 10) {
    final var batch = items.subList(i, Math.min(i + 10, items.size()));
    var batchResults = ctx.step(
        "process_batch_" + (i / 10),
        new TypeToken<List<Object>>() {},
        stepCtx -> batch.stream().map(item -> processItem(item)).toList()
    );
    results.addAll(batchResults);
}
return results;
