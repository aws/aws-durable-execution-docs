var results = new ArrayList<Map<String, Object>>();
for (var item : items) {
    try {
        var result = ctx.step("process-" + item, Map.class, stepCtx -> process(item));
        results.add(result);
    } catch (Exception e) {
        results.add(Map.of("error", e.getMessage()));
    }
}
