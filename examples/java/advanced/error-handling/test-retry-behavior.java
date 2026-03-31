@Test
void testRetrySuccess() {
    var handler = new RetryHandler();
    var runner = LocalDurableTestRunner.create(Map.class, handler);

    var result = runner.runUntilComplete(Map.of());

    // Should succeed after retries
    assertEquals(ExecutionStatus.SUCCEEDED, result.getStatus());
}
