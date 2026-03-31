@Test
void testHighValuePath() {
    var runner = LocalDurableTestRunner.create(Map.class, new ConditionalHandler());

    var result = runner.runUntilComplete(Map.of("amount", 1500));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    assertEquals("High-value order processed", result.getResult(String.class));

    // Verify approval step exists
    var approvalOp = result.getOperation("approval");
    assertNotNull(approvalOp);
}

@Test
void testStandardPath() {
    var runner = LocalDurableTestRunner.create(Map.class, new ConditionalHandler());

    var result = runner.runUntilComplete(Map.of("amount", 500));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Verify no approval step
    var approvalOp = result.getOperation("approval");
    assertNull(approvalOp);
}
