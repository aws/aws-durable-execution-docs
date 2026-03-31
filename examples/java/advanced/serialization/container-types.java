// Nested structures serialize automatically with Jackson
var settings = Map.of(
    "notifications", true,
    "theme", "dark",
    "limits", Map.of(
        "daily", new BigDecimal("500.00"),
        "monthly", new BigDecimal("10000.00")
    )
);

var complexData = Map.of(
    "user", Map.of(
        "id", UUID.randomUUID().toString(),
        "created", Instant.now().toString(),
        "balance", new BigDecimal("1234.56"),
        "tags", List.of("premium", "verified"),
        "settings", settings
    )
);

var result = ctx.step("process-user", Map.class, stepCtx -> processUser(complexData));
