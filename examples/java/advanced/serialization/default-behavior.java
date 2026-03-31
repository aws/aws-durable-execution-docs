// Jackson handles common types automatically
var result = ctx.step("process-order", Map.class, stepCtx -> {
    return Map.of(
        "orderId", UUID.randomUUID().toString(),
        "amount", new BigDecimal("99.99"),
        "timestamp", Instant.now().toString()
    );
});
