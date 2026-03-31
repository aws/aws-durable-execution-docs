@Test
void testRetryBehavior() {
    RetryHandler.attemptCount = 0;
    var runner = LocalDurableTestRunner.create(Map.class, new RetryHandler());

    var result = runner.runUntilComplete(Map.of());

    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
    assertEquals("Operation succeeded", result.getResult(String.class));
    assertTrue(RetryHandler.attemptCount >= 3);
}
