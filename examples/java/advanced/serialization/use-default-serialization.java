// Good - uses default Jackson serialization
var result = ctx.step("process-order", Map.class, stepCtx -> {
    return Map.of(
        "orderId", "ORD-123",
        "amount", new BigDecimal("99.99"),
        "timestamp", Instant.now().toString()
    );
});
