@Test
void testLongWait() {
    var runner = LocalDurableTestRunner.create(Map.class, new LongWaitHandler());

    var result = runner.runUntilComplete(Map.of());

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Verify wait exists
    var waitOp = result.getOperation("long_wait");
    assertNotNull(waitOp);
    assertEquals(OperationStatus.SUCCEEDED, waitOp.getStatus());
}
