@Test
void testCallbackCreation() {
    var runner = LocalDurableTestRunner.create(Map.class, new CallbackHandler());

    var result = runner.runUntilComplete(Map.of());

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    // Find callback operation
    var callbackOp = result.getOperation("example_callback");
    assertNotNull(callbackOp);
    assertEquals(OperationStatus.SUCCEEDED, callbackOp.getStatus());
}
