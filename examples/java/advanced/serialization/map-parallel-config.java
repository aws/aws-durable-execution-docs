var items = List.of(1, 2, 3, 4, 5);

// Custom serialization for map results
var config = MapConfig.builder()
    .serDes(new CustomSerDes())          // For the entire MapResult
    .itemSerDes(new ItemSerDes())         // For individual item results
    .build();

var result = ctx.map("process-items", items, Map.class,
    (item, stepCtx) -> processItem(item), config);

return Map.of("processed", result.getSucceeded().size());
