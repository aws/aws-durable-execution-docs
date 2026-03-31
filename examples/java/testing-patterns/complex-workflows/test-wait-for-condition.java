@Test
void testPolling() {
    var runner = LocalDurableTestRunner.create(Map.class,
        new WaitForConditionHandler());

    var result = runner.runUntilComplete(Map.of());

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    assertEquals(3, result.getResult(Integer.class));

    // Should have 3 increment steps
    var succeeded = result.getSucceededOperations().stream()
        .filter(op -> op.getName().startsWith("increment_"))
        .toList();
    assertEquals(3, succeeded.size());
}
