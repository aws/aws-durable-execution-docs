// Option 1: Reduce data
var smallData = Map.of("id", order.getId(), "status", order.getStatus());
var result = ctx.step("process-order", Map.class, stepCtx -> processOrder(smallData));

// Option 2: Use summary generator (for map operations)
var config = MapConfig.builder()
    .summaryGenerator(mapResult -> {
        return Map.of("count", mapResult.getAll().size());
    })
    .build();

var result = ctx.map("process-items", items, Map.class, (item, stepCtx) -> process(item), config);
