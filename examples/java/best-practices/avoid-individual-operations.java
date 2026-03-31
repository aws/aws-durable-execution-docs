var items = (List<Map<String, Object>>) input.get("items");
var results = new ArrayList<>();
// Creating a step for each item - too many checkpoints!
for (int i = 0; i < items.size(); i++) {
    final var item = items.get(i);
    var result = ctx.step("process_item_" + i, Object.class,
        stepCtx -> processItem(item));
    results.add(result);
}
return results;
