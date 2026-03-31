@Test
void testWait() {
    var runner = LocalDurableTestRunner.create(String.class, new WaitHandler());

    var result = runner.runUntilComplete("test");

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Find the wait operation
    var waitOp = result.getOperation("wait");
    assertNotNull(waitOp);
    assertEquals(OperationStatus.SUCCEEDED, waitOp.getStatus());
}
