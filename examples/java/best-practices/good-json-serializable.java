var result = ctx.step("process_order", Map.class, stepCtx -> {
    return Map.of(
        "order_id", order.get("id"),
        "total", 99.99,
        "items", List.of("item1", "item2"),
        "processed", true
    );
});
