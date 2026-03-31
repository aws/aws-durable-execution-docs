@Test
void testCallbackTimeout() {
    var runner = LocalDurableTestRunner.create(Map.class, new CallbackTimeoutHandler());

    var result = runner.runUntilComplete(Map.of());

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());

    var callbackOp = result.getOperation("approval_callback");
    assertNotNull(callbackOp);
    assertEquals(OperationStatus.SUCCEEDED, callbackOp.getStatus());
}
