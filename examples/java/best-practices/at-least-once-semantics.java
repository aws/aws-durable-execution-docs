// Safe to run multiple times - same input produces same output
var total = ctx.step("calculate_total", Double.class, stepCtx -> {
    List<Map<String, Object>> items = (List<Map<String, Object>>) input.get("items");
    return items.stream()
        .mapToDouble(item -> ((Number) item.get("price")).doubleValue())
        .sum();
});
return total;
