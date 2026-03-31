// Good - focused on one behavior
@Test
void testOrderValidationSucceeds() {
    var runner = LocalDurableTestRunner.create(Map.class, new OrderHandler());

    var result = runner.runUntilComplete(Map.of("order_id", "order-123"));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
}

@Test
void testOrderValidationFailsMissingId() {
    var runner = LocalDurableTestRunner.create(Map.class, new OrderHandler());

    var result = runner.runUntilComplete(Map.of());

    assertEquals(ExecutionStatus.FAILED, result.getStatus());
}

// Avoid - testing multiple behaviors in one test
@Test
void testOrderValidation() {
    var runner = LocalDurableTestRunner.create(Map.class, new OrderHandler());

    var result1 = runner.runUntilComplete(Map.of("order_id", "order-123"));
    assertEquals(ExecutionStatus.SUCCEEDED, result1.getStatus());

    var result2 = runner.runUntilComplete(Map.of());
    assertEquals(ExecutionStatus.FAILED, result2.getStatus());
}
