var productIds = List.of("prod_1", "prod_2", "prod_3", "prod_4");

// Get all inventory results
var batchResult = ctx.map("check-inventory", productIds, Map.class,
    (productId, index, childCtx) ->
        Map.of("product_id", productId, "in_stock", true, "quantity", 10));

// Filter to only in-stock products
var inStock = batchResult.results().stream()
    .filter(r -> Boolean.TRUE.equals(r.get("in_stock")))
    .map(r -> (String) r.get("product_id"))
    .collect(Collectors.toList());

return Map.of("in_stock", inStock);
