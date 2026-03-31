@Test
void testChildContext() {
    var runner = LocalDurableTestRunner.create(Map.class, new ChildContextHandler());

    var result = runner.runUntilComplete(Map.of());

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Find child context operation
    var contextOp = result.getOperation("child_operation");
    assertNotNull(contextOp);
    assertEquals(OperationStatus.SUCCEEDED, contextOp.getStatus());
}
