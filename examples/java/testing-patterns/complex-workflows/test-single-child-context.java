@Test
void testChildContext() {
    var runner = LocalDurableTestRunner.create(Map.class,
        new SingleChildContextHandler());

    var result = runner.runUntilComplete(Map.of("item_id", "item-123"));

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Check child context ran
    var contextOp = result.getOperation("item_processing");
    assertNotNull(contextOp);
    assertEquals(OperationStatus.SUCCEEDED, contextOp.getStatus());

    // Check child context result
    var childResult = contextOp.getStepResult(Map.class);
    assertEquals("item-123", childResult.get("item_id"));
}
