var result = ctx.step("process_order", Map.class, stepCtx -> {
    return Map.of(
        "order_id", order.get("id"),
        "total", new BigDecimal("99.99").doubleValue(),  // Convert to double
        "timestamp", Instant.now().toString()             // Convert to string
    );
});
