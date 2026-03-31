var items = (List<Map<String, Object>>) input.get("items");
var results = new ArrayList<>();
for (int i = 0; i < items.size(); i++) {
    final var item = items.get(i);
    var result = ctx.step(
        "process_item_" + i + "_" + item.get("id"), Object.class,
        stepCtx -> processItem(item));
    results.add(result);
}
return results;
