var result = ctx.step("process_order", Map.class, stepCtx -> {
    // Non-serializable types will cause checkpoint failures
    return Map.of(
        "order_id", order.get("id"),
        "total", new BigDecimal("99.99"),    // Won't serialize!
        "timestamp", LocalDateTime.now()      // Won't serialize!
    );
});
