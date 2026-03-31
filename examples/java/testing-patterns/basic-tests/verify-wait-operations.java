@Test
void testWaitOperation() {
    var runner = LocalDurableTestRunner.create(Map.class, new WaitHandler());

    var result = runner.runUntilComplete(Map.of());

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Find wait operation
    var waitOp = result.getOperation("wait");
    assertNotNull(waitOp);
    assertEquals(OperationStatus.SUCCEEDED, waitOp.getStatus());
}
